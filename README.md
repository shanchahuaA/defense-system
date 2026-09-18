# 毕业答辩管理系统

面向高校本科毕业答辩的成绩管理系统：超级管理员配置院系与评分指标，院系管理员建答辩小组并分配学生与教师，教师在线上给小组内学生打分，答辩组长用大模型生成小组评语并导出统分表，最终生成可下载的 Word 成绩表。

后端 Spring Boot + JPA，前端 Vue 3 + Element Plus，两者分离部署（开发时前端 Vite 代理到后端）。

> 发布说明：这是我在校期间的课程项目，发布在这里用于展示。**当前版本功能基本完整，但仍有已知缺陷**，已如实列在文末「已知问题与后续计划」——包括演示时会暴露的文档导出按钮。测试数据里的人和成绩都是虚构的。

## 目录结构

```
defense-system/
├── backend/                     Spring Boot 后端
│   ├── pom.xml
│   ├── src/main/java/com/defense3/demo/
│   │   ├── controller/          REST 接口
│   │   ├── service/             业务逻辑
│   │   ├── repository/          Spring Data JPA
│   │   ├── entity/ dto/         实体与传输对象
│   │   ├── security/            JWT 过滤器、UserDetails
│   │   ├── config/              安全、跨域、上传目录
│   │   └── utils/               Word 模板生成
│   ├── src/main/resources/
│   │   └── application-example.yml   配置模板（真正的 application.yml 不入库）
│   └── uploads/                 运行时上传目录（gitignore，仅保留空目录骨架）
├── frontend/                    Vue 3 + Vite 前端
│   └── src/{api,views,components,router,stores,layouts,styles,utils}
├── docs/
│   ├── design/                  建库脚本与测试数据 SQL
│   ├── requirements/            课程需求文档
│   ├── templates/               成绩表 Word 模板与提示词说明
│   └── testing-guide.md         手工功能测试指南
├── privacy_scan.py              推送前的隐私扫描脚本
└── README.md
```

## 技术栈

| 层 | 选型 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.4.1、Spring Web / Spring Data JPA / Spring Security |
| 认证 | JWT（jjwt 0.12.6），登录签发，请求头 `Authorization: Bearer <token>` |
| 数据库 | MySQL 8 + Hibernate（`ddl-auto: validate`，表结构由 SQL 脚本建） |
| 文档生成 | poi-tl 1.12.2（Word 模板渲染）、EasyExcel 4.0.3 |
| 大模型 | 通义千问（DashScope OpenAI 兼容接口），提示词模板存库、可在线编辑 |
| 前端 | Vue 3.5、Vite 6、Vue Router 4、Pinia 2、Element Plus 2.9、axios、Sass |
| 构建 | Maven（后端）、npm + Vite（前端） |

## 角色与功能

系统按四级权限组织。三种角色存在数据库里（`sys_user.role`）；**「答辩组长」不是第四种角色**，而是一名被指定为某个答辩小组 `leader_id` 的教师——登录时后端查当前答辩年份下他是否带组，把结果作为 `isGroupLeader` 一起返回。

### 超级管理员（`SUPER_ADMIN`）

- **院系管理**：增删改院系，创建院系管理员账号，上传系主任签名图片
- **管理员列表**：查看并管理部门管理员账号
- **评分指标配置**：分别配置论文类（3 项）与设计类（6 项）的评价指标内容与权值
- **模板管理**：上传各类成绩表的 Word 模板
- **AI 配置**：填写通义千问 API Key、编辑论文/设计两类评语提示词模板，可测试连接
- **年份管理**：设置答辩年份并指定当前年份（学生、分组、评分都按年份隔离）
- 另外可以使用「教师管理 / 学生管理」等院系管理员的功能

### 院系管理员（`DEPT_ADMIN`）

- **学生管理**：维护本系学生（学号、班级、题目、摘要、联系电话、邮箱），指定指导教师与评阅人
- **教师管理**：维护本系教师账号，新建教师时给初始密码，可重置密码、启停账号
- **答辩分组**：建答辩小组、指定组长与评委教师、把学生分到小组；也可一键启动大组答辩
- 可以给小组内学生打分

### 答辩组长（教师 + 带组）

- **AI 评语生成**：按小组逐学生调用大模型生成答辩小组评语，生成后可人工修改再保存
- **统分表**：查看本组每个教师的打分，导出《答辩小组统分表》，并查看大组答辩算出的调节系数

### 教师（`TEACHER`）

- **我指导的学生**：填写所指导学生的题目与摘要，录指导教师评定成绩，为本系学生指定评阅人
- **小组答辩评分**：按论文（3 项）/ 设计（6 项）填写分项成绩，系统累加总分；也可以只填总分由系统拆分到各小项
- **大组答辩评分**：给各组第一名打总评成绩
- **答辩记录**：填写答辩日期、地点、问题记录与答题情况
- **文档导出**：按小组导出答辩成绩表、成绩评定表 —— **当前不可用，见「已知问题」第 1 条**
- **个人中心**：上传手写签名图片（会嵌进导出的成绩表）、修改密码

## 环境要求

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| JDK | **17+** | Spring Boot 3.4 的下限。用 JDK 8 会直接编译失败 |
| Maven | 3.9+ | |
| MySQL | 8.x | |
| Node.js | 18+ | 前端构建 |

`mvn` 会用它自己的 `JAVA_HOME`，而不是 PATH 上的 `java`。如果机器上 `JAVA_HOME` 指向 JDK 8，先切到 17——值换成你机器上 JDK 17 的安装目录（Windows 上一般是 `Program Files\Java\jdk-17` 这种形式）：

```bat
:: cmd
set JAVA_HOME=<JDK 17 的安装目录>
```
```powershell
# PowerShell
$env:JAVA_HOME = '<JDK 17 的安装目录>'
```

## 建库步骤

在仓库根目录执行（`init.sql` 里已包含 `CREATE DATABASE`）：

```bash
mysql -u root -p < docs/design/init.sql               # 建库建表 + 评分指标 + 超管账号(admin/123456)
mysql -u root -p < docs/design/test_data.sql          # 5 个学院 / 10 个教师 / 10 个学生（虚构数据）
mysql -u root -p < docs/design/test_scoring_data.sql  # 答辩分组与小组评分
```

库名固定为 `defense_management`，如果改了库名，记得同步改连接串。

`docs/design/full_test_data.sql` 是另一套更小的英文数据集，用于验证文档导出流程；它在靠后的「AI Config」一节里 `TRUNCATE ai_config`，按需执行。

## 配置：数据库口令与 JWT 密钥

**真实的数据库口令与 JWT 签名密钥不在仓库里。** 仓库只提交模板 `backend/src/main/resources/application-example.yml`，真正的 `application.yml` 被 `.gitignore` 排除。

克隆后先复制一份：

```bash
cd backend/src/main/resources
cp application-example.yml application.yml
```

然后二选一提供真实值：

**做法 A（推荐）：用环境变量**，`application.yml` 保持 `${...}` 写法不动。设置方式就是普通的系统环境变量，`DB_URL` 这类非密钥项可以照下面这么写：

```bat
:: cmd
set DB_URL=jdbc:mysql://localhost:3306/defense_management
```
```powershell
# PowerShell
$env:DB_URL = "jdbc:mysql://localhost:3306/defense_management"
```

`DB_PASSWORD` 与 `JWT_SECRET` 同理，只是值换成你自己的口令与随机串——**这两个值不要写进任何会被提交的文件**。

**做法 B：直接改本地 `application.yml`**，把 `${DB_PASSWORD}` / `${JWT_SECRET}` 换成字面值。这个文件已被 gitignore，不会误提交。

可用的配置项：

| 环境变量 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `JWT_SECRET` | 是 | 无 | **至少 32 个字符**（HS256 密钥长度下限，见 `JwtUtils#getSigningKey`）。没设置时报 `Could not resolve placeholder 'JWT_SECRET'` |
| `DB_PASSWORD` | 否 | 空口令 | 不设置时按空口令连接（本机 MySQL 没有口令就正好）。**但别把配置里这一行整个删掉**——Spring 对解析不了的占位符不报错，而是把 `"${DB_PASSWORD}"` 这串字面量当口令发出去，报错会变成 `Access denied for user 'root'@'localhost' (using password: YES)`，看起来像口令填错了 |
| `DB_URL` | 否 | 本机 `defense_management` 库 | 完整 JDBC 连接串 |
| `DB_USERNAME` | 否 | `root` | |

上传目录有两个来源，别把它们当成一个：`file.upload.path`（默认 `${user.dir}/uploads`，相对 JVM 工作目录解析，从 `backend/` 启动时就是 `backend/uploads/`）只决定 `/uploads/**` 静态映射的根目录、以及启动时创建哪几个目录；而**手写签名实际写入的位置是写死的** `<JVM 工作目录>/uploads/signatures`（见「已知问题」第 11 条）。文档模板路径走 `file.upload.template-path`，这个是真读配置的。

## 启动步骤

两个进程，各开一个终端。**先起后端**（前端 Vite 会把 `/api` 和 `/uploads` 代理到 8080）。

**终端 1 —— 后端：**

```bash
cd backend
mvn spring-boot:run
```

看到 `Started DemoApplication` 即成功，服务在 `http://localhost:8080`。

**终端 2 —— 前端：**

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:5173`，用 `admin` / `123456` 登录（超管账号由 `init.sql` 建）。

想验证后端能构建出可执行 jar：

```bash
cd backend
mvn clean package
```

注意这个命令会跑唯一的那个测试（`DemoApplicationTests.contextLoads`），它是个 `@SpringBootTest`，**需要连得上数据库**——没建库或没配口令时会失败。只想编译就用 `mvn -DskipTests clean package`。

用到 JDK 17：`mvn` 走的是 `JAVA_HOME`，指向 JDK 8 时会编译失败。

## 推送前的隐私扫描

仓库根目录的 `privacy_scan.py` 会检查**一次提交会包含的所有文件**（工作树里没被 `.gitignore` 排除的文本文件），命中密码、密钥、连接串、个人信息或本机绝对路径时，列出 `文件:行号: 规则` 并以非零退出码结束：

```bash
python privacy_scan.py          # 扫描当前目录
python privacy_scan.py <目录>   # 扫描别处
```

它不回显命中的具体内容，所以报告可以随便粘贴；运行前不需要先 commit（它按 `.gitignore` 判断文件范围）。本仓库当前跑出来是**零命中**。

想让每次 `git push` 自动先扫一遍，把下面两行写进 `.git/hooks/pre-push`。hook 不在版本控制里，每个克隆都要各自装一次：

```sh
#!/bin/sh
exec python privacy_scan.py
```

**第一行不能省。** 实测：只写 `exec python privacy_scan.py` 时，Windows 上的 git 会尝试把 hook 当可执行文件启动，报 `error: cannot spawn .git/hooks/pre-push: No such file or directory` 并直接拒绝推送。本仓库的 hook 已按这两行装好（38 字节、无 BOM、LF 行尾，直接执行输出 `privacy_scan: clean`）；**这两行加这个脚本的拦截能力**另在一个临时仓库 + 本地 bare remote 上验过：干净的提交放行，带泄漏的提交被拒、远端一个 ref 都收不到。

**这个脚本有两条能力边界，推之前要知道：** 一是不认中文姓名（「姓+名」的正则在任何中文散文上都会误报，所以它干脆不扫）；二是不看二进制文件（文件头 8 KiB 里出现 NUL 字节就整份跳过），而 `.docx` / `.xlsx` 都是 zip——文档属性里的作者名它一个字也看不见。这两类只能人工核对：本仓库的测试 SQL 已逐条看过（都是张三 / 李四 / 王小明这类虚构名）；`docs/` 下 8 份 Office 文件也逐份解过 zip，其中 7 份的 `docProps/core.xml` 里带真人姓名，已改写成虚构姓名，正文未动；余下 `标准论文成绩表模板.docx` 只有生成器名 `Apache POI`，`评委表.xlsx` 的 `dc:creator` 是机器账号 `Administrator`——这两个都不是人名，保持原样。

## 已知问题与后续计划

### 已知问题

1. **文档导出功能没有接上后端，界面上的导出按钮导出不了文件。**
   `frontend/src/views/teacher/Documents.vue` 里，行内的「导出文档」下拉只弹一句提示、不产生文件（`handleExport` 里是 `TODO: 调用后端导出接口`）；工具栏的「批量导出」按钮被 `v-if="false"` 直接隐藏了。后端其实已经有 `DocumentGenerationService`（用 poi-tl 渲染模板）和 `TemplateDownloadController`，只是没接上。
   *后续计划：把这两个按钮接上已有的生成服务；接不上就先把它们从界面上摘掉，不要让演示时按下去没反应。*

2. **删除院系时没有关联数据校验，会连带删掉该院系下的所有学生与答辩小组。**
   `DepartmentService.delete()` 里只有一行 `// TODO: 检查是否有关联数据（教师、学生）` 就直接删了。而外键有三条：`student.department_id` 是 `ON DELETE CASCADE`、`defense_group.department_id` 也是 `ON DELETE CASCADE`、`sys_user.department_id` 是 `ON DELETE SET NULL`——删一个院系会静默级联删除它名下全部学生记录、全部答辩小组及其 `group_teacher` 关联行（教师则变成无院系）；学生没了，挂在他们身上的 `group_score` / `final_score` / `defense_record` 也随 `student_id` 的 CASCADE 一起消失。
   *后续计划：删除前先统计关联的教师、学生与答辩小组，非空则拒绝并给出提示；或改成软删除。*

3. **三个调试用的文档接口处于「功能暂时停用」状态。**
   `DocumentDownloadController` 与 `TemplateDownloadController` 上的 `@RequestMapping("/api/documents")` 被注释掉了（注释写着"功能暂时停用"），于是 `/test-generate`、`/download-standard-template`、`/debug-generate` 实际挂在根路径上，而不是 `/api/documents/*`。它们本是开发期的手工验证入口，其中 `/debug-generate` 还会写入写死的假数据。
   *后续计划：导出功能接通后一并清理或改回正常映射。*

4. **AI API Key 以明文存在数据库里，并且会原样返回给前端。**
   密钥存在 `ai_config.api_key`（明文列），`GET /api/admin/ai-config` 会把它读回前端显示。接口有 `@PreAuthorize("hasRole('SUPER_ADMIN')")`，只有超管能看，但落库和传输都没有加密。
   *后续计划：加密存储，返回值里脱敏成后四位。*

5. **新建教师与重置密码时的初始口令写死为 `123456`。**
   唯一的值在 `frontend/src/api/user.js` 的 `DEFAULT_INITIAL_CODE`，重置接口的默认参数与新建表单都用它；但界面文案里还散着几处写死的 `123456`（`Teachers.vue` 的输入框提示与重置确认框、`Admins.vue` 的重置确认框、登录页的管理员提示），改默认值时要一起改，否则文案会说谎。演示数据里所有账号也都是这个口令。
   *后续计划：由管理员逐个指定，或强制首次登录后修改。*

6. **测试只有一个，且它需要真实数据库。**
   除 `DemoApplicationTests.contextLoads` 之外没有测试代码；这一个也只是把 Spring 上下文拉起来，`ddl-auto: validate` 会去打数据库，所以没有库就跑不过。
   *后续计划：补业务层测试；至少把上下文测试改成用 H2 或 Testcontainers，让它不依赖本机库。*

7. **JWT 签发后无法吊销，有效期 24 小时。**
   改密码不会让已签发的令牌失效（`TokenBlacklist` 之类都没有），只能等它自己过期。

8. **前端构建产物不由后端托管。**
   后端没有把 `frontend/dist` 配成静态资源，所以必须前后端两个进程。要单进程部署得先加静态资源映射或把前端打进镜像。

9. **启动时对配置错误的提示是「延迟」的。**
   `ddl-auto: validate` 不会建表，所以库没建好时报的是 Hibernate 的表不存在错误，而不是一句"请先执行 init.sql"。

10. **上传接口的类型与大小校验只做在前端。**
    签名图「PNG/JPG、不超过 2MB」是 `Signature.vue` 里的前端校验；`UserService.uploadSignature` 只按原文件名取扩展名，不判断类型也不判断大小，服务端唯一的兜底是 multipart 的 10MB 上限。绕过前端直接调接口可以传更大的文件。
    *后续计划：把类型与大小校验补到服务端，不要只靠前端。*

11. **签名图片的落盘位置是写死的，不受配置控制。**
    `file.upload.path` 只被 `FileUploadConfig` 用来创建目录、以及把 `/uploads/**` 映射到它；真正写签名的两处（`UserService.uploadSignature`、`DepartmentService`）用的是 `Paths.get(System.getProperty("user.dir"), "uploads", "signatures")`，既不看 `file.upload.signature-path`，也不看 `file.upload.path`。于是照 `application-example.yml` 里那句「需要固定位置时用环境变量覆盖成绝对路径」做完，签名仍会写到 JVM 工作目录下，而 `/uploads/**` 已经指向别处——传上去的签名就取不到了。模板路径（`DocumentTemplateService` 读 `file.upload.template-path`）没有这个问题。
    *后续计划：把签名路径也改成读配置，或干脆去掉那两个配置项、只保留写死的约定，别让两边对不上。*

### 后续计划

- 接上文档导出（问题 1、3），这是功能上最大的缺口
- 补删除院系的关联校验（问题 2）
- 把 AI 密钥脱敏与加密（问题 4）
- 补测试（问题 6），并让上下文测试不依赖本机数据库
- 单进程部署包装（问题 8）
- 上传接口补服务端校验（问题 10）
- 统一上传目录的配置来源（问题 11）

## 许可证

[MIT](LICENSE)