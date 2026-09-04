# Mirror Wardrobe · 魔镜衣橱

一款面向个人的 **安卓智能衣橱管理 App**：把衣服拍进手机、按天气智能推荐穿搭，还能一键「AI 虚拟试衣」。

基于 Kotlin + Jetpack Compose 原生开发（MVP），数据本地优先，支持多设备云同步登录。

---

## 📸 功能演示

<img width="800" height="533" alt="Image" src="https://github.com/user-attachments/assets/a8edcca0-2a04-4b92-b77f-2e9eb2e8dc60" />

---

## ✨ 核心功能

- **智能衣橱管理**：为衣物添加照片、分类与「保暖值」，按分类整理收纳
- **穿搭搭配**：从衣柜中挑选单品自由组合，保存为整套 Look，可随时编辑
- **AI 虚拟试衣**：选定衣服与身形（默认模特 / 自己照片 / 现场拍照），AI 图生图生成上身穿搭效果，支持保存、分享与重新生成
- **天气智能推荐**：基于实时天气（温度 / 体感 / 风力 / 湿度 / 云量）计算今日保暖目标，优先从真实衣柜推荐穿搭，不足时给出通用建议
- **穿搭日历**：记录每日穿着，随时回看「今天穿了什么」
- **数据统计**：衣物总数、品类分布、单品穿着频率一目了然
- **账号系统**：邮箱注册 / 登录（Firebase Auth），数据随账号同步

## 🖼 主要页面

| 页面 | 说明 |
| --- | --- |
| 首页 | 天气卡片 + 今日保暖推荐 |
| 衣柜 | 衣物列表、添加 / 编辑衣物 |
| 穿搭 | 搭配创建、编辑、保存 |
| 日历 | 按日期记录并回顾穿搭 |
| 统计 | 分类分布（饼图）、穿着频率 |
| AI 魔镜 | 选衣 → 选身形 → 生成试衣效果 |

## 🛠 技术栈

- **语言 / UI**：Kotlin · Jetpack Compose · Material 3
- **架构**：MVVM（ViewModel + Repository + StateFlow）
- **本地存储**：Room（SQLite），本地优先、离线可用
- **网络**：Retrofit / OkHttp（AI 服务、天气）
- **图片**：Coil 加载 · CameraX 拍摄 · 图片压缩
- **定位 / 天气**：Fused Location + Geocoder · Open-Meteo（免费、免 Key）
- **AI 服务**：火山引擎 Ark 平台 · 豆包 Doubao 图像生成（图生图）
- **账号**：Firebase Auth（google-services.json）
- **构建**：Gradle Wrapper · JDK 17 · compileSdk 36

## 🏗 项目结构

```
app/src/main/java/com/comp7506/mywardrobe/
├── MainActivity.kt / MyWardrobeApp.kt / MyWardrobeApplication.kt   # 入口与导航骨架
├── navigation/AppRoutes.kt        # 页面路由定义
├── ui/
│   ├── screens/                   # Compose 页面（登录、首页、衣柜、穿搭、日历、统计、AI 试衣…）
│   ├── components/                # 可复用组件（卡片、日历、饼图、顶栏…）
│   ├── viewmodel/                 # 各页面 ViewModel（状态 + 业务协调）
│   └── theme/                     # 主题、颜色、字体
├── data/
│   ├── db/                        # Room 实体 / DAO / 数据库
│   ├── repository/                # 数据访问与 API 编排
│   ├── location/                  # 定位与逆地理编码
│   ├── weather/                   # 天气模型 / 仓库 / 客户端（Open-Meteo）
│   └── api/                       # AI 服务接口与配置
├── auth/                          # Firebase 认证管理
└── domain/recommendation/         # 保暖计算与穿搭推荐算法
```

## 🚀 环境要求与运行

**环境要求**

- Android Studio（推荐最新稳定版）
- Android SDK `platforms;android-36`（compileSdk = 36）
- JDK 17+（Android Studio 内置 JDK 即可，无需全局安装 Gradle）

**运行方式**

1. 用 Android Studio 打开项目根目录，等待 Gradle Sync 完成
2. 选择模拟器或真机，直接点击 Run

或命令行构建 Debug APK：

```powershell
.\gradlew :app:assembleDebug
```

产物路径：`app\build\outputs\apk\debug\app-debug.apk`

## ⚙️ 配置说明

- **账号登录**：`app/google-services.json`（Firebase 项目配置）**不随仓库提供**（含项目标识，防止误提交），需自行放入自己的 `google-services.json`——可从 Firebase 控制台下载，或沿用已有项目时从本地拷贝该文件到 `app/` 目录
- **AI 试衣**：默认接入火山引擎豆包图像生成。API Key 等敏感信息**不写入源码**：构建期从环境变量（或用户级 `~/.gradle/gradle.properties`）注入 `BuildConfig`，运行时经 `data/api/AIService.kt` 的 `APIConfig` 读取。

    | 变量 | 必填 | 说明 |
    | --- | --- | --- |
    | `DOUBAO_API_KEY` | 是 | 火山引擎 Ark API Key |
    | `DOUBAO_MODEL_ENDPOINT` | 是 | 模型推理接入点 ID（形如 `ep-2024xxxxxxxxx-xxxxx`） |
    | `DOUBAO_BASE_URL` | 否 | API 基础地址，默认 `https://ark.cn-beijing.volces.com/api/v3/`（其他区域或自建网关可覆盖） |

    配置方式二选一（未配置时 App 可正常构建，AI 试衣会提示"未配置"）：

    ① Windows 环境变量（仅当前命令行进程生效）：

    ```powershell
    $env:DOUBAO_API_KEY = "你的API_KEY"
    $env:DOUBAO_MODEL_ENDPOINT = "ep-你的终端ID"
    .\gradlew :app:assembleDebug
    ```

    ② 写入用户级 `%USERPROFILE%\.gradle\gradle.properties`（长期生效，Android Studio 直接 Run 亦可）：

    ```properties
    DOUBAO_API_KEY=你的API_KEY
    DOUBAO_MODEL_ENDPOINT=ep-你的终端ID
    ```
- **天气**：Open-Meteo 免费接口，无需申请 Key
- 若需更换其他支持图片输入的 AI 服务（Replicate / Stability 等），参见 `AI_FITTING_SETUP.md`

## 🌡 保暖推荐逻辑（简述）

以「体感舒适温度 26℃」为基准计算所需保暖度：

```
目标保暖度 = 26 - 体感温度（缺省时用实际气温），不低于 0
```

**环境修正**（累加，最多 +2）：

| 条件 | 加成 |
| --- | --- |
| 风速 ≥ 8 m/s | +1 |
| 湿度 ≥ 80% | +1 |
| 云量 ≥ 70% | +1 |

**推荐优先级**：优先按类别（外套 → 上衣 → 下装…）从真实衣柜贪心挑选单品凑足保暖度；衣柜数据不足时，回退到内置模板给出通用建议。

## 🧪 测试

单元测试重点覆盖 `HomeViewModel.refreshWeather` 的状态流转（成功 / 失败）、定位超时（>5s）与异常兜底逻辑：

```powershell
.\gradlew :app:testDebugUnitTest
```

## 📌 备注

- 本仓库为课程团队项目 MVP，以「先跑通、后打磨」为原则
- 数据采用本地优先策略（Room + 本地图片引用），登录后可跨设备同步
- 面向用户文案统一维护在 `res/values/strings.xml`，便于国际化扩展

---

*Made with ☕ and a lot of outfit planning.*
