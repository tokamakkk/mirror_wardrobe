# MyWardrobe UI 界面与组件说明

本文档基于当前代码中的 Jetpack Compose 页面实现，整理所有已接入导航的 UI 界面，以及每个界面的主要组件与可实现功能。

## 1. 全局结构

### 1.1 应用壳层（`MyWardrobeApp`）
- **核心组件**
  - `Scaffold`
  - `NavigationBar` + `NavigationBarItem`（底部主导航）
  - `NavHost` + `composable`（页面路由）
- **主要功能**
  - 根据登录状态在登录/注册页与主功能页之间自动跳转
  - 提供 5 个底部主入口：`Home`、`Wardrobe`、`Mirror`、`Outfits`、`Calendar`
  - 承载所有次级页面：`AddItem`、`CreateOutfit`、`EditOutfit`、`Stats`、`WarmthDetails`、`PortraitCapture`、`MirrorLoading`、`AiResult`

### 1.2 通用顶部栏（`AppTopBar`）
- **核心组件**
  - `TopAppBar`
  - 返回 `IconButton`（返回箭头）
  - 可选 `actions` 区域
- **主要功能**
  - 统一页面标题样式
  - 提供标准返回行为
  - 支持页面级自定义操作按钮（如网格/列表切换）

### 1.3 复用业务组件
- `ClothingItemGridCard`：衣物网格卡（图、名称、删除按钮）
- `ClothingItemListRow`：衣物列表行（图、名称、分类、删除按钮）
- `CategoryPieChart`：分类占比饼图（图形 + 图例百分比）
- `MonthCalendar`：月历组件（日期格、已记录日期标记、选中态）

---

## 2. 认证相关界面

## 2.1 登录页（`LoginScreen`）
- **页面组件**
  - `Column`（垂直布局）
  - 标题 `Text("Sign in")`
  - `Card` 表单容器
  - `OutlinedTextField`：邮箱输入
  - `OutlinedTextField`：密码输入（`PasswordVisualTransformation`）
  - 错误提示 `Text`
  - `Button("Sign in")`
  - `TextButton("No account? Register")`
- **可实现功能**
  - 输入邮箱密码并发起登录
  - 显示登录中的按钮状态与错误提示
  - 登录成功后自动跳转到首页
  - 跳转到注册页

## 2.2 注册页（`RegisterScreen`）
- **页面组件**
  - 与登录页结构基本一致
  - 标题 `Text("Register")`
  - `Button("Register")`
  - `TextButton("Already have an account? Sign in")`
- **可实现功能**
  - 创建账号（邮箱 + 密码）
  - 展示注册中状态与错误提示
  - 注册成功后自动进入首页
  - 返回登录页

---

## 3. 主导航界面

## 3.1 首页（`HomeScreen`）
- **页面组件**
  - 顶部头部区（应用名 + `Sign out` 文本按钮）
  - 天气卡片 `Card`（可点击进入 `WarmthDetails`）
  - 位置权限触发按钮（`Enable Weather` / `Grant permission`）
  - 天气状态文案与图标 `Icon`
  - 当日穿搭建议文本列表
  - 分类统计卡片 + `CategoryPieChart`
  - 最近穿搭卡片 + `LazyRow` 图片列表
- **可实现功能**
  - 请求定位权限并拉取本地天气
  - 根据天气展示温度、体感、风速、天气图标
  - 显示当日穿搭建议
  - 展示衣橱分类分布
  - 展示最近记录的穿搭缩略图
  - 退出登录并回到登录页

## 3.2 衣橱页（`WardrobeScreen`）
- **页面组件**
  - `Scaffold` + `AppTopBar("Wardrobe")`
  - 顶部操作 `IconButton`：网格/列表切换
  - `FloatingActionButton`：新增衣物
  - 搜索框 `OutlinedTextField("Search items…")`
  - 横向分类 `Tab`（All/Tops/Pants/Outerwear/Shoes/Accessories）
  - 网格视图：`LazyVerticalGrid` + `ClothingItemGridCard`
  - 列表视图：`LazyColumn` + `ClothingItemListRow`
  - 删除确认 `AlertDialog`
- **可实现功能**
  - 按名称搜索衣物
  - 按分类筛选衣物
  - 网格与列表两种浏览模式切换
  - 进入新增衣物页
  - 删除衣物（带二次确认）

## 3.3 魔镜页（`MirrorScreen`）
- **页面组件**
  - 顶部预览区域（默认人形图）
  - 返回按钮、模式切换按钮组（`默认形象` / `我的照片`）
  - `Next` 按钮
  - 底部可拖拽相册区（高度动画）
  - `LazyRow` 缩略图列表（首项为相机入口）
  - 多选覆盖层（选中打勾图标）
- **可实现功能**
  - 在“默认形象”和“我的照片”模式间切换
  - 跳转到拍照/选图页面（`PortraitCapture`）
  - 从衣橱中多选试穿衣物
  - 进入 AI 生成加载页（携带衣物 ID 与模式参数）
  - 未选择衣物时提示用户

## 3.4 套装页（`OutfitsScreen`）
- **页面组件**
  - `Scaffold` + `AppTopBar("Outfits")`
  - 顶部操作 `IconButton`：网格/列表切换
  - `FloatingActionButton`：新建套装
  - 搜索框 `OutlinedTextField("Search occasions…")`
  - 场景筛选 `Tab`（All/Business/Office/Dating/Casual/Ceremony/Sport/homewear）
  - 网格卡片（封面图 + 名称 + 场景）
  - 列表卡片（封面图 + 名称 + 场景 + 件数 + 删除按钮）
  - 删除确认 `AlertDialog`
- **可实现功能**
  - 按关键字搜索套装
  - 按场景筛选套装
  - 切换网格/列表视图
  - 跳转创建套装页
  - 点击套装进入编辑页
  - 删除套装（带确认）

## 3.5 日历页（`CalendarScreen`）
- **页面组件**
  - `Scaffold` + `AppTopBar("Outfit calendar")`
  - 月份切换区（左右箭头 + 当前年月）
  - `MonthCalendar` 月历面板（记录点标记）
  - 当日穿搭预览 `Card`（显示选中日期套装图）
  - `Button("Log today's outfit")`
  - `Button("Remove day record")`
  - 记录删除确认 `AlertDialog`
  - 记录方式选择 `AlertDialog`
    - `Take photo` 按钮
    - 已保存套装按钮列表
- **可实现功能**
  - 浏览不同月份并选择具体日期
  - 查看选中日期已记录穿搭
  - 用“拍照”方式记录当日穿搭
  - 用“已保存套装”记录当日穿搭
  - 删除某天穿搭记录（不删除原套装）

---

## 4. 衣物/套装编辑相关界面

## 4.1 新增衣物页（`AddItemScreen`）
- **页面组件**
  - `Scaffold` + `AppTopBar("Add item")`
  - 图片来源区 `Card`
    - `Button("Camera")`
    - `Button("Gallery")`
    - 图片预览 `AsyncImage`
  - 信息表单区 `Card`
    - 名称输入 `OutlinedTextField`
    - 分类下拉 `ExposedDropdownMenuBox` + `DropdownMenu`
    - 保暖值下拉 `ExposedDropdownMenuBox` + `DropdownMenu`
    - 错误提示 `Text`
    - `Button("Save")`
- **可实现功能**
  - 调相机拍照并保存临时图片
  - 从相册选择图片
  - 填写名称、分类、保暖值
  - 提交保存新衣物
  - 保存成功后自动返回上一页

## 4.2 创建套装页（`CreateOutfitScreen`）
- **页面组件**
  - `Scaffold` + `AppTopBar("Create outfit")`
  - 套装画布 `Card`（已选衣物 `LazyRow` + 移除按钮）
  - 套装名输入 `OutlinedTextField`
  - 场景下拉 `ExposedDropdownMenuBox`
  - 可选衣物网格 `LazyVerticalGrid`
  - 选中态覆盖层（打勾图标）
  - 错误提示 `Text`
  - `Button("Save")`
- **可实现功能**
  - 从全衣橱多选衣物组成套装
  - 设置套装名称与场景
  - 对已选衣物进行移除
  - 保存新套装并返回

## 4.3 编辑套装页（`EditOutfitScreen`）
- **页面组件**
  - `AppTopBar("Edit outfit")`
  - 加载态文本 `Text("Loading…")`
  - 套装已选衣物区 `Card`（`LazyRow` + 移除按钮）
  - 套装名输入 `OutlinedTextField`
  - 可选衣物网格 `LazyVerticalGrid`
  - 错误提示 `Text`
  - `Button("Save changes")`
- **可实现功能**
  - 加载并展示指定套装详情
  - 修改套装名称
  - 增删套装内衣物
  - 保存套装变更并返回

---

## 5. 天气与统计界面

## 5.1 统计页（`StatsScreen`）
- **页面组件**
  - `Scaffold` + `AppTopBar("Wardrobe stats")`
  - 总数卡片 `Card`（Total items）
  - 分类统计卡片 `Card` + `CategoryPieChart`
  - 穿着频率标题 + `LazyColumn` 频率卡片列表
- **可实现功能**
  - 展示衣物总数
  - 展示各分类占比
  - 展示单件衣物穿着频次排行信息

## 5.2 保暖与场景详情页（`WarmthDetailsScreen`）
- **页面组件**
  - `Scaffold` + `AppTopBar("Warmth & occasion")`
  - 当日建议卡片（`Today outfit suggestion`）
  - 规则说明卡片（保暖值计算逻辑 + 参考值）
  - 场景筛选下拉 `ExposedDropdownMenuBox`
  - 已保存套装列表 `Card`（图、名称、场景、总保暖值）
- **可实现功能**
  - 承接天气推荐并展示文字建议
  - 解释保暖目标计算方式
  - 按场景筛选匹配套装
  - 点击套装进入编辑页

---

## 6. AI 试衣流程界面

## 6.1 人像采集页（`PortraitCaptureScreen`）
- **页面组件**
  - CameraX 预览 `AndroidView(PreviewView)`
  - 返回按钮、`Next` 按钮
  - 中央拍照按钮（圆形相机按钮）
  - 引导提示文本（全身照）
  - 底部可拖拽“Recent Photos”区（`LazyRow`）
  - 相机错误兜底页（错误文案 + `Go Back`）
- **可实现功能**
  - 请求相机权限并初始化相机预览
  - 拍照并将照片加入可选列表
  - 从最近照片中选择人像
  - 将选中人像 URI 回传给 `MirrorScreen`
  - 异常时提示并允许返回

## 6.2 AI 加载页（`MirrorLoadingScreen`）
- **页面组件**
  - 全屏渐变背景 `Box`
  - 文案 `Text`（带淡入动画）
  - `LinearProgressIndicator`
- **可实现功能**
  - 读取已选衣物 ID 参数
  - 加载默认人像底图
  - 调用 AI 生成试衣图
  - 生成成功后自动跳转 `AiResultScreen`
  - 失败时 Toast 提示并回退上一页

## 6.3 AI 结果页（`AiResultScreen`）
- **页面组件**
  - 顶部工具栏（返回 + 标题）
  - 加载态区（`CircularProgressIndicator` + 文案 + 线性进度条）
  - 错误态区（错误文本 + 返回按钮）
  - 结果图区域 `AsyncImage`
  - 已选衣物横向列表 `LazyRow` + `ClothingItemCard`
  - 底部操作栏
    - `Button("保存")`
    - `Button("分享")`
    - `OutlinedButton("重新生成")`
- **可实现功能**
  - 展示 AI 试衣生成结果图
  - 展示本次参与生成的衣物列表
  - 保存结果图到系统图库
  - 分享结果图
  - 回到 Mirror 重新生成

---

## 7. 页面清单（按路由）

- `login` -> `LoginScreen`
- `register` -> `RegisterScreen`
- `home` -> `HomeScreen`
- `wardrobe` -> `WardrobeScreen`
- `add_item` -> `AddItemScreen`
- `outfits` -> `OutfitsScreen`
- `create_outfit` -> `CreateOutfitScreen`
- `edit_outfit/{outfitId}` -> `EditOutfitScreen`
- `calendar` -> `CalendarScreen`
- `stats` -> `StatsScreen`
- `warmth_details` -> `WarmthDetailsScreen`
- `mirror` -> `MirrorScreen`
- `portrait_capture` -> `PortraitCaptureScreen`
- `mirror_loading?clothingIds={...}&useImageUpload={...}` -> `MirrorLoadingScreen`
- `ai_result?imageUrl={...}&clothingIds={...}` -> `AiResultScreen`

以上即当前代码中已实现并接入导航的全部 UI 界面说明。
