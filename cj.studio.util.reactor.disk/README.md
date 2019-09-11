# 数据文件
为了实现类似区块链的分布式账本式的真实意义的分布式存储，本项目实现两类存储：
 一种是顺序块存储，一种是哈希存储
- 适用范围
前者用于反应器持久化队列的实现，后者用于cluster反应器进行分布式存储和查询。
## 实现原理
-- 哈希存储以一致性哈希实现
-- 顺序块存储以单链表实现
## 定向分布存储
用于形成记忆路由，定向存储用于替代一致性哈希路由由于节点变化而导致同一key变化了导向路径。使用哈希存储实现在反应器节点，先查hash是否存在节点，不存在则根据后继节点分布，然后存入hash

## 能省则省
内前的key-value库非常之多之好，因此直接采用，地址：
git clone https://github.com/linkedin/PalDB.git  能遍历
git clone https://github.com/orhanobut/hawk.git  更简单
git clone https://github.com/jankotek/JDBM3.git 带事务,能遍历，功能丰富。支持链表、treeMap等算法