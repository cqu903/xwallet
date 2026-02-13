const { chromium } = require('playwright');

(async () => {
  console.log('启动浏览器测试...');
  
  try {
    const browser = await chromium.launch({ 
      headless: true,
      channel: 'chromium',
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    const page = await browser.newPage();
    
    console.log('正在访问 Google...');
    await page.goto('https://www.google.com', { waitUntil: 'networkidle' });
    
    const title = await page.title();
    const url = page.url();
    
    console.log('✓ 页面标题:', title);
    console.log('✓ 页面 URL:', url);
    
    if (title.toLowerCase().includes('google')) {
      console.log('✓ 测试通过：成功打开 Google 页面');
    } else {
      console.log('✗ 测试失败：页面标题不包含 Google');
    }
    
    await page.screenshot({ path: 'google-test.png', fullPage: false });
    console.log('✓ 截图已保存到: google-test.png');
    
    await browser.close();
    console.log('\n✓ Playwright 工作正常！');
    
    process.exit(0);
  } catch (error) {
    console.error('✗ 测试失败:', error.message);
    process.exit(1);
  }
})();
