#!/usr/bin/env python3
"""
批量更新HTML文件：将内联CSS替换为外部样式表引用
"""
import re
import os

# 所有需要保留的页面特定样式（当这些样式在</style>之前时）
PAGE_SPECIFIC_STYLES = {
    'layout.html': '''
    /* 页面特定样式 */
    .sidebar-float {
      display: flex;
      flex-direction: column;
    }
    .bubble {
      position: absolute;
      border-radius: 50%;
      background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.8), rgba(255,255,255,0.1));
      backdrop-filter: blur(8px);
      -webkit-backdrop-filter: blur(8px);
      border: 1px solid rgba(255, 255, 255, 0.3);
      animation: floatBubble linear infinite;
    }
    .bubble-1 { width: 80px; height: 80px; top: 10%; left: 5%; animation-duration: 18s; opacity: 0.6; }
    .bubble-2 { width: 120px; height: 120px; top: 60%; left: 80%; animation-duration: 25s; opacity: 0.5; animation-delay: 3s; }
    .bubble-3 { width: 60px; height: 60px; top: 70%; left: 15%; animation-duration: 20s; opacity: 0.7; animation-delay: 6s; }
    .bubble-4 { width: 100px; height: 100px; top: 20%; left: 70%; animation-duration: 22s; opacity: 0.55; animation-delay: 9s; }
    .bubble-5 { width: 50px; height: 50px; top: 45%; left: 45%; animation-duration: 15s; opacity: 0.65; animation-delay: 12s; }
    [data-mode="dark"] .bubble {
      background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.15), rgba(255,255,255,0.05));
      border-color: rgba(255, 255, 255, 0.1);
    }
    @keyframes floatBubble {
      0%, 100% { transform: translate(0, 0) scale(1); }
      25% { transform: translate(20px, -30px) scale(1.05); }
      50% { transform: translate(-10px, -50px) scale(0.95); }
      75% { transform: translate(15px, -20px) scale(1.02); }
    }
    ''',
    'dashboard.html': '''
    /* 页面特定样式 */
    .card-stat::before {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      width: 140px;
      height: 140px;
      background: linear-gradient(135deg, var(--theme-primary), var(--theme-secondary));
      opacity: 0.08;
      border-radius: 0 0 0 100%;
      transition: transform 0.4s ease;
    }
    .card-stat:hover::before {
      transform: scale(1.15);
    }
    ''',
    # 其他页面暂时没有特定样式
}

# HTML模板头部（替换原来内联的CSS部分）
HTML_HEAD_TEMPLATE = '''  <!-- Google Fonts -->
  <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Inter:wght@400;500;600&display=swap" rel="stylesheet"/>
  <!-- Material Symbols -->
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
  <!-- 公共样式表 -->
  <link rel="stylesheet" href="shop-common.css"/>
'''

def get_page_specific_style(filename):
    """获取页面特定样式"""
    return PAGE_SPECIFIC_STYLES.get(filename, '')

def process_html_file(filepath):
    """处理单个HTML文件"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 检查是否已经有 shop-common.css 引用
    if 'shop-common.css' in content:
        print(f"  [SKIP] {os.path.basename(filepath)} - already has shop-common.css")
        return False
    
    # 找到 <style> 标签开始位置
    style_match = re.search(r'<style>\s*:root\s*\{', content)
    if not style_match:
        print(f"  [SKIP] {os.path.basename(filepath)} - no :root style found")
        return False
    
    style_start = style_match.start()
    
    # 找到 </style> 结束位置
    style_end = content.find('</style>', style_start)
    if style_end == -1:
        print(f"  [SKIP] {os.path.basename(filepath)} - no </style> found")
        return False
    
    style_end = style_end + len('</style>')
    
    # 获取页面特定样式
    page_style = get_page_specific_style(os.path.basename(filepath))
    
    # 构建新的 <style> 标签
    new_style_block = f'\n  <style>\n    {page_style}\n  </style>\n'
    
    # 替换
    new_content = content[:style_start] + new_style_block + content[style_end:]
    
    # 确保在 <head> 中有正确的资源引用顺序
    # 检查是否有正确的资源引用
    if '<!-- Google Fonts -->' not in new_content:
        # 在 <style> 后面添加资源引用
        insert_pos = new_content.find('</style>') + len('</style>')
        new_content = new_content[:insert_pos] + '\n' + HTML_HEAD_TEMPLATE.strip() + '\n' + new_content[insert_pos:]
    
    # 写回文件
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"  [OK] {os.path.basename(filepath)}")
    return True

def main():
    html_dir = os.path.dirname(os.path.abspath(__file__))
    html_files = [
        os.path.join(html_dir, 'categories.html'),
        os.path.join(html_dir, 'dashboard.html'),
        os.path.join(html_dir, 'index.html'),
        # layout.html 已处理
        os.path.join(html_dir, 'messages.html'),
        os.path.join(html_dir, 'myshop.html'),
        os.path.join(html_dir, 'orders.html'),
        os.path.join(html_dir, 'products.html'),
        os.path.join(html_dir, 'profile.html'),
        os.path.join(html_dir, 'users.html'),
    ]
    
    print("批量更新HTML文件CSS引用...")
    for filepath in html_files:
        if os.path.exists(filepath):
            process_html_file(filepath)
        else:
            print(f"  [NOT FOUND] {os.path.basename(filepath)}")
    print("完成!")

if __name__ == '__main__':
    main()
