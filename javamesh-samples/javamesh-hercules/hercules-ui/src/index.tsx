/* eslint-disable no-template-curly-in-string */
import { ConfigProvider } from 'antd';
import React from 'react'
import ReactDOM from 'react-dom'
import { BrowserRouter } from 'react-router-dom';

import App from './App'
import {ContextProvider} from './ContextProvider';
import zhCN from 'antd/lib/locale/zh_CN';
import "./mock"

ReactDOM.render(<BrowserRouter>
    <ConfigProvider locale={zhCN} form={{
      validateMessages: {
        types: {
          integer: "${label}不是一个有效的整数"
        }
      }
    }}><ContextProvider>
        <App />
      </ContextProvider>
    </ConfigProvider>
  </BrowserRouter>, document.getElementById('root'));
