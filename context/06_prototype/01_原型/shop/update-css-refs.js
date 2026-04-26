#!/usr/bin/env node
/**
 * 批量更新HTML文件：将内联CSS替换为外部样式表引用
 */

const fs = require('fs');
const path = require('path');

const SHOP_DIR = path.join(__dirname);

// 页面特定样式
const PAGE_SPECIFIC_STYLES = {
    'layout.html': `
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
    `,
    'dashboard.html': `
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
    `,
};

const FILES_TO_SKIP = ['layout.html', 'shop-common.css']; // layout已处理

function processHtmlFile(filepath) {
    const filename = path.basename(filepath);
    
    if (FILES_TO_SKIP.includes(filename)) {
        console.log(`  [SKIP] ${filename} - already processed`);
        return false;
    }
    
    let content = fs.readFileSync(filepath, 'utf-8');
    
    // 检查是否已经有 shop-common.css 引用
    if (content.includes('shop-common.css')) {
        console.log(`  [SKIP] ${filename} - already has shop-common.css`);
        return false;
    }
    
    // 找到 <style> 标签
    // 支持两种格式: <style>\n:root { 和 <style>\n/* Theme Variables */\n:root {
    const styleMatch = content.match(/<style>\s*(?:\/\*[^*]*\*\/)?\s*:root\s*\{/);
    if (!styleMatch) {
        console.log(`  [SKIP] ${filename} - no :root style found`);
        return false;
    }
    
    const styleStart = content.indexOf('<style>');
    const styleEnd = content.indexOf('</style>') + '</style>'.length;
    
    if (styleStart === -1 || styleEnd === -1) {
        console.log(`  [SKIP] ${filename} - style tags not found`);
        return false;
    }
    
    // 获取页面特定样式
    const pageStyle = PAGE_SPECIFIC_STYLES[filename] || '';
    
    // 构建新的 <style> 标签
    const newStyleBlock = `\n  <style>\n    ${pageStyle}\n  </style>\n`;
    
    // 替换 - 在 </style> 后插入公共样式引用
    const beforeStyle = content.substring(0, styleEnd);
    const afterStyle = content.substring(styleEnd);
    
    // 检查是否有公共样式引用
    let newContent;
    if (afterStyle.includes('<!-- Google Fonts -->')) {
        // 已有引用，不需要再添加
        newContent = beforeStyle + newStyleBlock + afterStyle;
    } else {
        // 在 </style> 后添加公共样式引用
        const commonStylesRef = `  <!-- Google Fonts -->
  <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Inter:wght@400;500;600&display=swap" rel="stylesheet"/>
  <!-- Material Symbols -->
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
  <!-- 公共样式表 -->
  <link rel="stylesheet" href="shop-common.css"/>
`;
        newContent = beforeStyle + newStyleBlock + commonStylesRef + afterStyle;
    }
    
    fs.writeFileSync(filepath, newContent, 'utf-8');
    console.log(`  [OK] ${filename}`);
    return true;
}

function main() {
    console.log('批量更新HTML文件CSS引用...\n');
    
    const files = fs.readdirSync(SHOP_DIR)
        .filter(f => f.endsWith('.html'))
        .map(f => path.join(SHOP_DIR, f));
    
    let updated = 0;
    for (const filepath of files) {
        if (processHtmlFile(filepath)) {
            updated++;
        }
    }
    
    console.log(`\n完成! 更新了 ${updated} 个文件`);
}

main();
