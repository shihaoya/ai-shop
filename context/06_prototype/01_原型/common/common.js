/**
 * 多店铺积分商城系统 - 公共 JavaScript
 *
 * 版本：v1.0
 *
 * 包含：Tailwind 配置、Vue 主题切换逻辑、导航菜单状态管理
 */

/* ============================================
   一、Tailwind CSS 配置
   ============================================ */

(function() {
  if (typeof tailwind !== 'undefined') {
    tailwind.config = {
      darkMode: 'class',
      theme: {
        extend: {
          colors: {
            primary: '#004ac6',
            secondary: '#712ae2',
            'on-surface': '#191b23',
            'on-surface-variant': '#434655',
            outline: '#737686',
            success: '#10B981',
            warning: '#F59E0B',
            danger: '#EF4444',
            'glass-bg': 'rgba(255, 255, 255, 0.6)',
            'glass-border': 'rgba(255, 255, 255, 0.3)',
          },
          borderRadius: {
            '3xl': '2.5rem',
          },
          fontFamily: {
            display: ['Manrope', 'system-ui', 'sans-serif'],
            headline: ['Manrope', 'system-ui', 'sans-serif'],
            body: ['Inter', 'system-ui', 'sans-serif'],
          },
        },
      },
    };
  }
})();

/* ============================================
   二、Vue 主题切换应用
   ============================================ */

(function() {
  const { createApp, ref } = Vue;

  /**
   * 初始化主题应用
   * @param {Object} options 配置选项
   * @param {Array} options.navItems 导航菜单项
   * @param {Function} options.onNavChange 导航切换回调
   */
  function initThemeApp(options) {
    const defaultOptions = {
      navItems: [
        { name: 'Dashboard', icon: 'dashboard', active: true },
        { name: '店铺管理', icon: 'storefront', active: false },
        { name: '用户管理', icon: 'group', active: false },
        { name: '订单管理', icon: 'receipt_long', active: false },
        { name: '消息通知', icon: 'notifications', active: false },
        { name: '个人中心', icon: 'person', active: false }
      ],
      onNavChange: null
    };

    const config = { ...defaultOptions, ...options };

    const app = createApp({
      setup() {
        const navItems = ref(config.navItems);
        const searchQuery = ref('');
        const showThemePanel = ref(false);
        const isDark = ref(false);

        // 设置当前激活的导航项
        const setActive = (index) => {
          navItems.value.forEach((item, i) => {
            item.active = i === index;
          });
          if (config.onNavChange) {
            config.onNavChange(index, navItems.value[index]);
          }
        };

        // 设置主题颜色
        const setTheme = (theme) => {
          if (theme) {
            document.documentElement.setAttribute('data-theme', theme);
          } else {
            document.documentElement.removeAttribute('data-theme');
          }
        };

        // 切换暗色模式
        const toggleDarkMode = () => {
          isDark.value = !isDark.value;
          if (isDark.value) {
            document.documentElement.setAttribute('data-mode', 'dark');
          } else {
            document.documentElement.removeAttribute('data-mode');
          }
        };

        // 检查并恢复保存的主题状态
        const restoreThemeState = () => {
          const savedTheme = localStorage.getItem('theme-color');
          const savedDark = localStorage.getItem('theme-dark');

          if (savedTheme) {
            setTheme(savedTheme);
          }
          if (savedDark === 'true') {
            isDark.value = true;
            document.documentElement.setAttribute('data-mode', 'dark');
          }
        };

        // 保存主题状态
        const saveThemeState = () => {
          const currentTheme = document.documentElement.getAttribute('data-theme');
          localStorage.setItem('theme-color', currentTheme || '');
          localStorage.setItem('theme-dark', isDark.value.toString());
        };

        // 在主题或模式改变时保存状态
        const originalSetTheme = setTheme;
        const originalToggleDarkMode = toggleDarkMode;

        return {
          navItems,
          searchQuery,
          showThemePanel,
          isDark,
          setActive,
          setTheme: (...args) => {
            originalSetTheme(...args);
            saveThemeState();
          },
          toggleDarkMode: (...args) => {
            originalToggleDarkMode(...args);
            saveThemeState();
          },
          restoreThemeState
        };
      }
    });

    return app;
  }

  // 暴露到全局
  window.ThemeApp = {
    init: initThemeApp
  };

  // 如果 DOM 已准备好，自动初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      if (window.Vue) {
        window.ThemeApp = { init: initThemeApp };
      }
    });
  } else {
    if (window.Vue) {
      window.ThemeApp = { init: initThemeApp };
    }
  }
})();

/* ============================================
   三、工具函数
   ============================================ */

(function() {
  /**
   * 显示 Toast 提示
   * @param {string} message 提示消息
   * @param {number} duration 显示时长(ms)
   */
  function showToast(message, duration = 2000) {
    // 移除已存在的 toast
    const existingToast = document.querySelector('.custom-toast');
    if (existingToast) {
      existingToast.remove();
    }

    // 创建 toast 元素
    const toast = document.createElement('div');
    toast.className = 'custom-toast';
    toast.innerHTML = `
      <div style="
        position: fixed;
        top: 24px;
        left: 50%;
        transform: translateX(-50%);
        padding: 12px 24px;
        background: var(--color-glass-bg);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1px solid var(--color-glass-border);
        border-radius: 16px;
        box-shadow: var(--shadow-lg);
        z-index: 99999;
        font-family: var(--font-body);
        font-size: 14px;
        color: var(--color-on-surface);
        animation: slideDown 0.3s ease;
      ">
        ${message}
      </div>
    `;

    document.body.appendChild(toast);

    // 添加动画样式
    if (!document.querySelector('#toast-animation-styles')) {
      const style = document.createElement('style');
      style.id = 'toast-animation-styles';
      style.textContent = `
        @keyframes slideDown {
          from {
            opacity: 0;
            transform: translateX(-50%) translateY(-20px);
          }
          to {
            opacity: 1;
            transform: translateX(-50%) translateY(0);
          }
        }
      `;
      document.head.appendChild(style);
    }

    // 自动移除
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  }

  /**
   * 复制文本到剪贴板
   * @param {string} text 要复制的文本
   * @returns {Promise<boolean>}
   */
  async function copyToClipboard(text) {
    try {
      await navigator.clipboard.writeText(text);
      showToast('复制成功');
      return true;
    } catch (err) {
      console.error('复制失败:', err);
      return false;
    }
  }

  // 暴露到全局
  window.Utils = {
    showToast,
    copyToClipboard
  };
})();

/* ============================================
   四、默认导航配置（layout.html 使用）
   ============================================ */

window.DEFAULT_NAV_ITEMS = [
  { name: 'Dashboard', icon: 'dashboard', active: true },
  { name: '店铺管理', icon: 'storefront', active: false },
  { name: '用户管理', icon: 'group', active: false },
  { name: '订单管理', icon: 'receipt_long', active: false },
  { name: '消息通知', icon: 'notifications', active: false },
  { name: '个人中心', icon: 'person', active: false }
];
