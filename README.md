从 FooIbar 的 EhViewer 修改而来，加回了内置的 cronet 以使用 host_resolver_rules 规避 DNS 污染，以及使用 QUIC 实现直连。

由于部分魔改系统对预测性返回手势的阉割，可能会导致本应用产生闪退，此闪退与本应用无关且无法修复，请克隆back分支自行编译不支持预测性返回手势的应用。

- 在 Cronet 使用 host\_resolver\_rules 规避 DNS 污染
- 恢复 OkHttp 引擎、内置 Hosts、域前置和 DoH (https://github.com/FooIbar/EhViewer/issues/12#issuecomment-1713695222)
- 恢复 Cookie 登录 (https://github.com/FooIbar/EhViewer/issues/692#issuecomment-1929373085)
- 支持登月账号登录 (https://github.com/FooIbar/EhViewer/issues/134#issuecomment-1784012743)
- 在 OkHttp 支持 ECH

## 下载

本分支不提供Release版本下载。

请自行编译或前往 [Actions](//github.com/lings03/EhViewer/actions/workflows/ci.yml) 下载最新 CI 版本。

| 变种          | 功能                    |
|-------------|-----------------------|
| Default     | Android 9+, 完全支持      |
| Marshmallow | Android 6.0-8.1, 有限支持 |

## 缺陷和功能请求

**本分支为自用类型，不接受除bug以外的任何功能请求。**
