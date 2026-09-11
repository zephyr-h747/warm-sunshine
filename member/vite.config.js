import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import pxToRem from 'postcss-pxtorem'

// 会员端：移动端 rem 适配（375 设计稿），/api 代理到后端 8080
// host: 监听 0.0.0.0，允许同一局域网内的手机通过 http://<电脑IP>:5173 访问
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  css: {
    postcss: {
      plugins: [
        // px → rem（rootValue 37.5 = 375 设计稿 / 10）：配合 index.html 的根字号脚本，
        // 手机端渲染与原 px→vw 方案逐像素等价；宽屏（PC）根字号封顶后画布限宽居中
        pxToRem({
          rootValue: 37.5,
          unitPrecision: 5,
          propList: ['*'],
          selectorBlackList: ['.ignore-'],
          replace: true,
          mediaQuery: false,
          minPixelValue: 1
        })
      ]
    }
  }
})
