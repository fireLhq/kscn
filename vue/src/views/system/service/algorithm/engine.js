import installAnimations from './animation';
import installAlgorithm from './AlgorithmLibrary/Algorithm';
import installGraph from './AlgorithmLibrary/Graph';
import installHash from './AlgorithmLibrary/Hash';
import installRecursive from './AlgorithmLibrary/Recursive';
const algorithmLoaders = {
    "AVL": () => import(/* webpackChunkName: "algorithm-AVL" */ "./AlgorithmLibrary/AVL.js"),
    "BFS": () => import(/* webpackChunkName: "algorithm-BFS" */ "./AlgorithmLibrary/BFS.js"),
    "BinomialQueue": () => import(/* webpackChunkName: "algorithm-BinomialQueue" */ "./AlgorithmLibrary/BinomialQueue.js"),
    "BPlusTree": () => import(/* webpackChunkName: "algorithm-BPlusTree" */ "./AlgorithmLibrary/BPlusTree.js"),
    "BST": () => import(/* webpackChunkName: "algorithm-BST" */ "./AlgorithmLibrary/BST.js"),
    "BTree": () => import(/* webpackChunkName: "algorithm-BTree" */ "./AlgorithmLibrary/BTree.js"),
    "BucketSort": () => import(/* webpackChunkName: "algorithm-BucketSort" */ "./AlgorithmLibrary/BucketSort.js"),
    "ChangingCoordinate2D": () => import(/* webpackChunkName: "algorithm-ChangingCoordinate2D" */ "./AlgorithmLibrary/ChangingCoordinate2D.js"),
    "ChangingCoordinate3D": () => import(/* webpackChunkName: "algorithm-ChangingCoordinate3D" */ "./AlgorithmLibrary/ChangingCoordinate3D.js"),
    "ClosedHash": () => import(/* webpackChunkName: "algorithm-ClosedHash" */ "./AlgorithmLibrary/ClosedHash.js"),
    "ClosedHashBucket": () => import(/* webpackChunkName: "algorithm-ClosedHashBucket" */ "./AlgorithmLibrary/ClosedHashBucket.js"),
    "ComparisonSort": () => import(/* webpackChunkName: "algorithm-ComparisonSort" */ "./AlgorithmLibrary/ComparisonSort.js"),
    "ConnectedComponent": () => import(/* webpackChunkName: "algorithm-ConnectedComponent" */ "./AlgorithmLibrary/ConnectedComponent.js"),
    "CountingSort": () => import(/* webpackChunkName: "algorithm-CountingSort" */ "./AlgorithmLibrary/CountingSort.js"),
    "DFS": () => import(/* webpackChunkName: "algorithm-DFS" */ "./AlgorithmLibrary/DFS.js"),
    "DijkstraPrim": () => import(/* webpackChunkName: "algorithm-DijkstraPrim" */ "./AlgorithmLibrary/DijkstraPrim.js"),
    "DisjointSet": () => import(/* webpackChunkName: "algorithm-DisjointSet" */ "./AlgorithmLibrary/DisjointSet.js"),
    "DPChange": () => import(/* webpackChunkName: "algorithm-DPChange" */ "./AlgorithmLibrary/DPChange.js"),
    "DPFib": () => import(/* webpackChunkName: "algorithm-DPFib" */ "./AlgorithmLibrary/DPFib.js"),
    "DPLCS": () => import(/* webpackChunkName: "algorithm-DPLCS" */ "./AlgorithmLibrary/DPLCS.js"),
    "FibonacciHeap": () => import(/* webpackChunkName: "algorithm-FibonacciHeap" */ "./AlgorithmLibrary/FibonacciHeap.js"),
    "Floyd": () => import(/* webpackChunkName: "algorithm-Floyd" */ "./AlgorithmLibrary/Floyd.js"),
    "Heap": () => import(/* webpackChunkName: "algorithm-Heap" */ "./AlgorithmLibrary/Heap.js"),
    "HeapSort": () => import(/* webpackChunkName: "algorithm-HeapSort" */ "./AlgorithmLibrary/HeapSort.js"),
    "Kruskal": () => import(/* webpackChunkName: "algorithm-Kruskal" */ "./AlgorithmLibrary/Kruskal.js"),
    "LeftistHeap": () => import(/* webpackChunkName: "algorithm-LeftistHeap" */ "./AlgorithmLibrary/LeftistHeap.js"),
    "OpenHash": () => import(/* webpackChunkName: "algorithm-OpenHash" */ "./AlgorithmLibrary/OpenHash.js"),
    "QueueArray": () => import(/* webpackChunkName: "algorithm-QueueArray" */ "./AlgorithmLibrary/QueueArray.js"),
    "QueueLL": () => import(/* webpackChunkName: "algorithm-QueueLL" */ "./AlgorithmLibrary/QueueLL.js"),
    "RadixSort": () => import(/* webpackChunkName: "algorithm-RadixSort" */ "./AlgorithmLibrary/RadixSort.js"),
    "RadixTree": () => import(/* webpackChunkName: "algorithm-RadixTree" */ "./AlgorithmLibrary/RadixTree.js"),
    "RecFact": () => import(/* webpackChunkName: "algorithm-RecFact" */ "./AlgorithmLibrary/RecFact.js"),
    "RecQueens": () => import(/* webpackChunkName: "algorithm-RecQueens" */ "./AlgorithmLibrary/RecQueens.js"),
    "RecReverse": () => import(/* webpackChunkName: "algorithm-RecReverse" */ "./AlgorithmLibrary/RecReverse.js"),
    "RedBlack": () => import(/* webpackChunkName: "algorithm-RedBlack" */ "./AlgorithmLibrary/RedBlack.js"),
    "RotateScale2D": () => import(/* webpackChunkName: "algorithm-RotateScale2D" */ "./AlgorithmLibrary/RotateScale2D.js"),
    "RotateScale3D": () => import(/* webpackChunkName: "algorithm-RotateScale3D" */ "./AlgorithmLibrary/RotateScale3D.js"),
    "RotateTranslate2D": () => import(/* webpackChunkName: "algorithm-RotateTranslate2D" */ "./AlgorithmLibrary/RotateTranslate2D.js"),
    "Search": () => import(/* webpackChunkName: "algorithm-Search" */ "./AlgorithmLibrary/Search.js"),
    "SimpleStack": () => import(/* webpackChunkName: "algorithm-SimpleStack" */ "./AlgorithmLibrary/SimpleStack.js"),
    "SkewHeap": () => import(/* webpackChunkName: "algorithm-SkewHeap" */ "./AlgorithmLibrary/SkewHeap.js"),
    "SplayTree": () => import(/* webpackChunkName: "algorithm-SplayTree" */ "./AlgorithmLibrary/SplayTree.js"),
    "StackArray": () => import(/* webpackChunkName: "algorithm-StackArray" */ "./AlgorithmLibrary/StackArray.js"),
    "StackLL": () => import(/* webpackChunkName: "algorithm-StackLL" */ "./AlgorithmLibrary/StackLL.js"),
    "TopoSortDFS": () => import(/* webpackChunkName: "algorithm-TopoSortDFS" */ "./AlgorithmLibrary/TopoSortDFS.js"),
    "TopoSortIndegree": () => import(/* webpackChunkName: "algorithm-TopoSortIndegree" */ "./AlgorithmLibrary/TopoSortIndegree.js"),
    "Trie": () => import(/* webpackChunkName: "algorithm-Trie" */ "./AlgorithmLibrary/Trie.js"),
    "TST": () => import(/* webpackChunkName: "algorithm-TST" */ "./AlgorithmLibrary/TST.js")
};

const baseInstallers = [installAnimations, installAlgorithm];
const dependencies = { Graph: installGraph, Hash: installHash, Recursive: installRecursive };

// Translate UI labels only. Radio/checkbox values and algorithm input data stay unchanged.
const controlLabels = new Map(Object.entries({
    'Insert': '插入', 'Delete': '删除', 'Find': '查找', 'Print': '输出',
    'Clear': '清空', 'Clear Heap': '清空堆', 'Remove Smallest': '删除最小值',
    'Bubble Sort': '冒泡排序', 'Insertion Sort': '插入排序', 'Selection Sort': '选择排序',
    'Quick Sort': '快速排序', 'Merge Sort': '归并排序', 'Shell Sort': '希尔排序',
    'Bucket Sort': '桶排序', 'Counting Sort': '计数排序', 'Radix Sort': '基数排序', 'Heap Sort': '堆排序',
    'Randomize Array': '随机生成数组', 'Randomize List': '随机生成数据',
    'New Graph': '随机生成图', 'Small Graph': '小规模图', 'Large Graph': '大规模图',
    'Directed Graph': '有向图', 'Undirected Graph': '无向图',
    'Adjacency List Representation': '邻接表表示', 'Adjacency Matrix Representation': '邻接矩阵表示',
    'Logical Representation': '逻辑结构', 'Internal Representation': '内部结构',
    'Run BFS': '运行广度优先搜索', 'Run DFS': '运行深度优先搜索',
    'Run Dijkstra': '运行 Dijkstra 算法', 'Run Prim': '运行 Prim 算法',
    'Run Kruskal': '运行 Kruskal 算法', 'Run Floyd-Warshall': '运行 Floyd 算法',
    'Run Connected Component': '查找连通分量', 'Do Topological Sort': '拓扑排序',
    'Start Vertex:': '起始顶点：', 'Union': '合并集合', 'Union By Rank': '按秩合并',
    'Path Compression': '路径压缩', 'Rank = # of nodes': '以节点数作为秩',
    'Rank = estimated height': '以估计高度作为秩',
    'Show Null Leaves': '显示空叶节点', 'Show Null Path Lengths': '显示零路径长',
    'Preemtive Split / Merge (Even max degree only)': '预先分裂/合并（仅偶数阶）',
    'Factorial': '计算阶乘', 'Queens': '求解 N 皇后', 'Reverse': '反转', 'Board size:  (1-8)': '棋盘大小（1—8）：',
    'Fibonacci Recursive': '递归求斐波那契数', 'Fibonacci Table': '动态规划求斐波那契数',
    'Fibonacci Memoized': '记忆化求斐波那契数', 'Change Recursive': '递归找零',
    'Change Table': '动态规划找零', 'Change Memoized': '记忆化找零', 'Change Greedy': '贪心找零',
    'LCS Recursive': '递归求最长公共子序列', 'LCS Table': '动态规划求最长公共子序列', 'LCS Memoized': '记忆化求最长公共子序列',
    'S1:': '字符串一：', 'S2:': '字符串二：',
    'Change Shape': '切换图形', 'Change Size': '更改大小', 'Transform': '执行变换',
    'Transform Point': '变换坐标点', 'Move Objects': '移动对象', 'Show all axes': '显示全部坐标轴',
    'Rotate': '旋转', 'Scale': '缩放', 'Rotate, then scale': '先旋转再缩放', 'Scale, then rotate': '先缩放再旋转',
    'Rotation Angle': '旋转角度', 'X Angle': 'X 轴旋转角度', 'Y Angle': 'Y 轴旋转角度', 'Z Angle': 'Z 轴旋转角度',
    'Scale X': 'X 轴缩放', 'Scale Y': 'Y 轴缩放', 'Scale Z': 'Z 轴缩放',
    'Translate X': 'X 轴平移', 'Translate Y': 'Y 轴平移', 'Row Major': '行优先', 'Column Major': '列优先',
    '+y Up': 'Y 轴向上', '+y Down': 'Y 轴向下',
    'Hand Space -> World Space': '手部坐标 → 世界坐标', 'World Space -> Hand Space': '世界坐标 → 手部坐标'
}));

function translateLabel(value) {
    const text = String(value);
    const label = text.trim();
    return controlLabels.has(label) ? text.replace(label, controlLabels.get(label)) : text;
}

const canvasLabels = new Map(Object.entries({
    'XXX': '', 'World Space': '世界坐标系', 'Local Space': '局部坐标系',
    'Robot Space to World Space': '机器人坐标系 → 世界坐标系',
    'World Space to Robot Space': '世界坐标系 → 机器人坐标系',
    'Hand Space to Robot Space': '手部坐标系 → 机器人坐标系',
    'Robot Space to Hand Space': '机器人坐标系 → 手部坐标系',
    '(Orientation)': '（方向）', '(Position)': '（位置）',
    'Vertex': '顶点', 'Visited': '已访问', 'Known': '已确定', 'Parent': '父节点',
    'Path': '路径', 'No Path': '无路径', 'Cost': '代价', 'Path Table': '路径表', 'Cost Table': '代价表',
    'BFS Queue': '广度优先搜索队列', 'Sequence': '序列', 'Topological Order': '拓扑序列',
    'Indegree': '入度', 'Zero Indegree Vertices': '零入度顶点', 'Disjoint Set': '并查集',
    'Min element': '最小元素', 'NextElem': '下一元素', 'Coins to Use': '可用硬币',
    '# of Coins Required': '所需硬币数量', 'Required Coins:': '所需硬币：',
    'Amount remaining:': '剩余金额：', 'Current String:': '当前字符串：',
    'Output:': '输出：', 'Finding:': '查找：', 'Seaching for:': '查找：',
    'Hashing:': '哈希计算：', 'Inserting;': '插入：', 'Deleting:': '删除：',
    'Linked List Array index =': '链表数组下标 =', '<deleted>': '〈已删除〉',
    'Return Value = ""': '返回值 = ""', 'Return Value = 1': '返回值 = 1',
    'Return Value = false': '返回值 = false', 'Return Value = true': '返回值 = true',
    'Creating a new node': '创建新节点', 'Creating a new root': '创建新根节点',
    'Printing tree': '输出树中元素', 'Backing up ...': '回退中……',
    'Unwinding Recursion': '递归回溯', 'Unwinding recursion.': '递归回溯',
    'Adjusting height after recursive call': '递归返回后更新高度',
    'Single Rotate Left': '单次左旋', 'Single Rotate Right': '单次右旋',
    'Double Rotate Left': '双次左旋', 'Double Rotate Right': '双次右旋',
    'Zig Left': '左旋', 'Zig Right': '右旋', 'Zig-Zag Left': '左侧异向双旋',
    'Zig-Zag Right': '右侧异向双旋', 'Zig-Zig Left': '左侧同向双旋', 'Zig-Zig Right': '右侧同向双旋',
    'Splay inserted element to root of tree': '将插入节点伸展到根节点',
    'Splaying found node to root of tree': '将找到的节点伸展到根节点',
    'Splay largest element in left tree to root': '将左子树最大节点伸展到根节点',
    'Removing root, leaving left and right trees': '删除根节点，保留左右子树',
    'Remove root element, leaving two subtrees': '删除根节点，保留两棵子树',
    'No right tree, make left tree the root.': '没有右子树，将左子树作为新根',
    'No left tree, make right tree the root.': '没有左子树，将右子树作为新根',
    'Left tree now has no right subtree, connect left and right trees': '左子树已无右分支，连接左右子树',
    'Node to delete is a leaf.  Delete it.': '待删除节点是叶节点，直接删除',
    'Node to delete has no left child.': '待删除节点没有左子节点',
    'Set parent of deleted node to right child of deleted node.': '将父节点连接到待删除节点的右子节点',
    'Node to delete has no right child.': '待删除节点没有右子节点',
    'Set parent of deleted node to left child of deleted node.': '将父节点连接到待删除节点的左子节点',
    'Node to delete has two childern.': '待删除节点有两个子节点',
    'Find largest node in left subtree.': '查找左子树的最大节点',
    'Copy largest value of left subtree into node to delete.': '将左子树最大值复制到待删除节点',
    'Remove node whose value we copied.': '删除已复制其值的原节点',
    'Found null tree, inserting element': '找到空子树，插入元素',
    'Found null tree (or phantom leaf), inserting element': '找到空子树或虚拟叶节点，插入元素',
    'Node now contains too many keys.  Splittig ...': '节点关键字过多，正在分裂……',
    'Node has too few keys.  Stealing from left sibling.': '节点关键字过少，向左兄弟借关键字',
    'Stealing from right sibling': '向右兄弟借关键字', 'Merge the two subtrees': '合并两棵子树',
    'Reconnecting tree after merge': '合并后重新连接树',
    'Checking to see if tree to left of element to delete': '检查待删除关键字左侧的子树',
    'has an extra key': '是否有多余关键字',
    'Tree to left of element to delete does have': '待删除关键字左侧的子树有',
    'an extra key. Finding the largest key in that subtree ...': '多余关键字，查找其中的最大关键字……',
    'Tree to left of element to delete does not have an extra key.': '左侧子树没有多余关键字',
    'Looking to the right ...': '检查右侧子树……',
    'Tree to right of element to delete does have an extra key.': '右侧子树有多余关键字',
    'Finding the smallest key in that subtree ...': '查找其中的最小关键字……',
    'Neither subtree has extra nodes.  Mergeing around the key to delete,': '两棵子树均无多余节点，围绕待删除关键字合并，',
    'and recursively deleting ...': '然后递归删除……',
    'Root of the tree is red.  Color it black': '根节点为红色，将其染黑',
    'Tree root is red, color it black.': '根节点为红色，将其染黑',
    'Deleted node was red.  No tree rotations required.': '删除的是红节点，无需旋转',
    'Back node removed.  Increasing child\'s blackness level': '删除了黑节点，增加子节点的黑色层级',
    'Coloring child of deleted node black': '将被删除节点的子节点染黑',
    'Coloring \'Null Leaf\' double black': '将空叶节点标记为双黑',
    'Double black node is root.  Make it single black.': '双黑节点是根节点，将其改为单黑',
    'Double black node has red sibling.  Rotate tree to make sibling black ...': '双黑节点的兄弟为红色，旋转并将兄弟染黑……',
    'Double black node has black sibling and 2 black nephews.  Push up black level': '兄弟及其两个子节点均为黑色，将黑色层级向上传递',
    'Double black node has black sibling, but double black node is a left child,': '双黑节点及其兄弟均为黑色，双黑节点是左子节点，',
    'and the right nephew is black.  Rotate tree to make opposite nephew red ...': '右侄节点为黑色，旋转使另一侧侄节点变红……',
    'Double black node has black sibling, but double black node is a right child,': '双黑节点及其兄弟均为黑色，双黑节点是右子节点，',
    'and the left nephew is black.  Rotate tree to make opposite nephew red ...': '左侄节点为黑色，旋转使另一侧侄节点变红……',
    'Double black node has black sibling, is a left child, and its right nephew is red.': '双黑节点的兄弟为黑色，双黑节点在左，右侄节点为红色',
    'Double black node has black sibling, is a right child, and its left nephew is red.': '双黑节点的兄弟为黑色，双黑节点在右，左侄节点为红色',
    'Pushing up black level created another double black node.  Repeating ...': '向上传递后出现新的双黑节点，继续处理……',
    'One rotation can fix double-blackness.': '一次旋转即可消除双黑',
    'Node and parent are both red.  Uncle of node is red -- push blackness down from grandparent': '节点、父节点及叔节点均为红色，将祖父节点的黑色向下传递',
    'Node and parent are both red.  Node is left child, parent is left child': '节点与父节点均为红色，且均为左子节点',
    'Node and parent are both red.  Node is right child, parent is right child': '节点与父节点均为红色，且均为右子节点',
    'Can fix extra redness with a single rotation': '一次旋转即可消除连续红节点',
    'Node and parent are both red.  Node is left child, parent is right child -- rotate': '节点与父节点均为红色，节点在左、父节点在右，进行旋转',
    'Node and parent are both red.  Node is right child, parent is left child -- rotate': '节点与父节点均为红色，节点在右、父节点在左，进行旋转',
    'Create a heap with one node, merge with existing heap.': '创建单节点堆，再与已有堆合并',
    'Merging node': '合并节点', 'Merging left heap with empty heap, return left heap': '左堆与空堆合并，返回左堆',
    'Merging right heap with empty heap, return right heap': '右堆与空堆合并，返回右堆',
    'Min element is in left heap.  Recursively merge right subtree of left heap with right heap': '最小元素在左堆，将左堆的右子树与右堆递归合并',
    'Min element is in right heap.  Recursively merge right subtree of right heap with left heap': '最小元素在右堆，将右堆的右子树与左堆递归合并',
    'Left subtree has Null Path Length at least as large as right subtree.  No swap required ...': '左子树零路径长不小于右子树，无需交换……',
    'Right subtree has larger Null Path Length than left subtree.  Swapping ...': '右子树零路径长较大，交换左右子树……',
    'Swapping subtrees after merge ...': '合并后交换子树……',
    'Empty tree found.   String not in the tree': '树为空，未找到字符串',
    'String not in the tree, nothing to delete': '树中不存在该字符串，无需删除',
    'Reached null tree': '到达空子树', 'Word is not in the tree': '树中不存在该单词',
    'Found string to delete, setting node to "False"': '找到待删除字符串，将节点标记为 false',
    'Node is "False", string is not in tree': '节点标记为 false，树中不存在该字符串',
    'Node is "True", string is in tree': '节点标记为 true，树中存在该字符串',
    'Reached the end of the prefix stored at this node': '已到达当前节点所存前缀的末尾',
    'Reached a mismatch in prefix.': '前缀不匹配', 'Create a new node with common prefix': '以公共前缀创建新节点',
    'Connect new node to the old, and reset prefix stored at previous node': '连接新旧节点，并更新原节点的前缀',
    'Reached end of search string,': '已到达待查找字符串末尾，',
    'Still characters remaining at node': '当前节点仍有剩余字符', 'String not in tree': '树中不存在该字符串',
    'Reached the end of the string': '已到达字符串末尾', 'Current node is False': '当前节点标记为 false',
    'Current node is True': '当前节点标记为 true', 'Word is Not the tree': '树中不存在该单词',
    'Word is in the tree': '树中存在该单词', 'Set current node to true': '将当前节点标记为 true',
    'Reached the end of the string.  Check if current node is "True"': '已到达字符串末尾，检查当前节点是否标记为 true',
    'Reached the end of the string.  Set Current node to "True"': '已到达字符串末尾，将当前节点标记为 true',
    'Clearning up after delete ...': '删除后清理……',
    'Clerning up after delete ...': '删除后清理……',
    'Leaf at end of word, no more cleanup required': '该叶节点是单词末尾，无需继续清理',
    'Tree has center child, no more cleanup required': '节点存在中间子节点，无需继续清理',
    'Deletion left us with a "False" leaf': '删除后留下标记为 false 的叶节点', 'Removing false leaf': '删除该叶节点',
    'Deletion left us with a "False" node': '删除后留下标记为 false 的节点',
    'Containing one child.  Combining ...': '该节点只有一个子节点，正在合并……',
    'Comparing next letter in search term': '比较待查找字符串的下一个字符',
    'to next letter in prefix of current node': '与当前节点前缀的下一个字符',
    'Next character in string  matches character at current node': '下一字符与当前节点字符相同',
    'Recursively look at center child,': '递归检查中间子节点，',
    'removing first letter from search string': '并移除待查找字符串的首字符',
    'Next character in string < Character at current node': '下一字符小于当前节点字符',
    'Recursively look at left node,': '递归检查左子节点，', 'leaving search string as it is': '待查找字符串保持不变',
    'Next character in string > Character at current node': '下一字符大于当前节点字符',
    'Recursively look at left right,': '递归检查右子节点，',
    'Reached a leaf without a character, still have characeters left in search string': '已到达空字符叶节点，但仍有字符未匹配',
    'String is not in the tree': '树中不存在该字符串',
    'Calculate this.indegree of all verticies by going through every edge of the graph': '遍历所有边，计算每个顶点的入度',
    'Collect all vertices with 0 this.indegree onto a stack': '将所有零入度顶点入栈',
    'Pop off top vertex with this.indegree 0, add to topological sort': '弹出栈顶零入度顶点，加入拓扑序列',
    'Vertices in the same tree.  Skip edge': '两个顶点已在同一棵树中，跳过此边',
    'Finding Cheapest Uknown Vertex': '查找代价最小的未确定顶点',
    'Setting known field to True': '将该顶点标记为已确定',
    'Finding Paths in Table': '在表中查找路径', 'Creating tree from table': '根据表创建树'
}).map(([english, chinese]) => [english.replace(/\s+/g, ' ').trim(), chinese]));

function translateCanvasText(value, userText) {
    const text = String(value);
    if (userText.has(text)) return text;
    const key = text.replace(/\s+/g, ' ').trim();
    if (canvasLabels.has(key)) return canvasLabels.get(key);
    // Recognize explanatory messages, keeping variable values and expressions verbatim.
    const patterns = [
        [/^Searching for (.*) : (.*) \(look to left subtree\)$/, (_, value, comparison) => `查找 ${value}：${comparison}（进入左子树）`],
        [/^Searching for (.*) : (.*) \(look to right subtree\)$/, (_, value, comparison) => `查找 ${value}：${comparison}（进入右子树）`],
        [/^Searching for (.*) : (.*) \(Element found!?\)$/, (_, value, comparison) => `查找 ${value}：${comparison}（已找到）`],
        [/^Searching for (.*) :.*\(Element not found\)$/, (_, value) => `查找 ${value}：未找到`],
        [/^Searching for (.*)$/, (_, value) => `查找 ${value}`],
        [/^Inserting[:; ]+(.*)$/, (_, value) => `插入：${value}`],
        [/^Deleting[: ]+(.*)$/, (_, value) => `删除：${value}`],
        [/^Finding (.*) and splaying to roo+t$/, (_, value) => `查找 ${value}，并将其伸展到根节点`],
        [/^Finding[: ]+(.*)$/, (_, value) => `查找：${value}`],
        [/^Element (.*?) not found\.?$/, (_, value) => `未找到元素 ${value}`],
        [/^Element (.*?) is not in the tree$/, (_, value) => `树中不存在元素 ${value}`],
        [/^(?:Element|Elemet) (.*?) not found, could not delete$/, (_, value) => `未找到元素 ${value}，无法删除`],
        [/^"(.*?)" not Found$/, (_, value) => `未找到“${value}”`],
        [/^String (.*?) not found$/, (_, value) => `未找到字符串 ${value}`],
        [/^Vertex (\d+)$/, (_, value) => `顶点 ${value}`],
        [/^Return Value = (.*)$/, (_, value) => `返回值 = ${value}`]
    ];
    for (const [pattern, replacement] of patterns) if (pattern.test(key)) return key.replace(pattern, replacement);
    return text;
}

function attachCanvasPresentation(runtime, world) {
    const canvas = runtime.document.getElementById('canvas');
    const ctx = world.ctx;
    const restorers = [];
    let bounds = null;
    let offsetX = 0, offsetY = 0;
    const include = (x, y, padding = 0) => {
        if (!bounds || ctx.globalAlpha === 0 || !Number.isFinite(x) || !Number.isFinite(y)) return;
        bounds.left = Math.min(bounds.left, x - padding);
        bounds.right = Math.max(bounds.right, x + padding);
        bounds.top = Math.min(bounds.top, y - padding);
        bounds.bottom = Math.max(bounds.bottom, y + padding);
    };
    const patch = (name, wrap) => {
        const original = ctx[name];
        if (typeof original !== 'function') return;
        const descriptor = Object.getOwnPropertyDescriptor(ctx, name);
        const replacement = wrap((...args) => original.apply(ctx, args));
        ctx[name] = replacement;
        restorers.push(() => {
            if (ctx[name] !== replacement) return;
            if (descriptor) Object.defineProperty(ctx, name, descriptor);
            else delete ctx[name];
        });
    };
    patch('measureText', original => text => original(translateCanvasText(text, runtime.userText)));
    ['fillText', 'strokeText'].forEach(name => patch(name, original => (text, x, y, ...args) => {
        const translated = translateCanvasText(text, runtime.userText);
        if (bounds && translated.trim()) {
            const metrics = ctx.measureText(translated);
            const fontSize = parseFloat(ctx.font) || 10;
            const ascent = Number.isFinite(metrics.actualBoundingBoxAscent) ? metrics.actualBoundingBoxAscent
                : ctx.textBaseline === 'top' ? 0 : ctx.textBaseline === 'middle' ? fontSize / 2 : fontSize;
            const descent = Number.isFinite(metrics.actualBoundingBoxDescent) ? metrics.actualBoundingBoxDescent : fontSize - ascent;
            const left = ctx.textAlign === 'center' ? x - metrics.width / 2 : ctx.textAlign === 'right' ? x - metrics.width : x;
            const padding = name === 'strokeText' ? (Number(ctx.lineWidth) || 1) / 2 : 0;
            include(left, y - ascent, padding);
            include(left + metrics.width, y + descent, padding);
        }
        return original(translated, x, y, ...args);
    }));
    ['moveTo', 'lineTo'].forEach(name => patch(name, original => (x, y) => {
        include(x, y, (Number(ctx.lineWidth) || 1) / 2 + 1);
        return original(x, y);
    }));
    ['rect', 'fillRect', 'strokeRect'].forEach(name => patch(name, original => (x, y, width, height) => {
        const padding = name === 'strokeRect' ? (Number(ctx.lineWidth) || 1) / 2 + 1 : 0;
        include(x, y, padding); include(x + width, y + height, padding);
        return original(x, y, width, height);
    }));
    patch('arc', original => (x, y, radius, ...args) => {
        const padding = (Number(ctx.lineWidth) || 1) / 2 + 1;
        include(x - radius, y - radius, padding); include(x + radius, y + radius, padding);
        return original(x, y, radius, ...args);
    });
    ['quadraticCurveTo', 'bezierCurveTo'].forEach(name => patch(name, original => (...args) => {
        for (let i = 0; i < args.length; i += 2) include(args[i], args[i + 1], (Number(ctx.lineWidth) || 1) / 2 + 1);
        return original(...args);
    }));
    const originalDraw = world.draw;
    const drawScene = () => {
        ctx.save();
        try {
            ctx.setTransform(1, 0, 0, 1, 0, 0);
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.translate(offsetX, offsetY);
            bounds = { left: Infinity, right: -Infinity, top: Infinity, bottom: -Infinity };
            originalDraw.call(world);
            return bounds;
        } finally { bounds = null; ctx.restore(); }
    };
    const draw = function() {
        const frame = world.framenum;
        const content = drawScene();
        if (!Number.isFinite(content.bottom)) return;
        const nextX = Math.max(offsetX, content.left < 0 ? Math.ceil(-content.left) + 12 : 0);
        const nextY = Math.max(offsetY, content.top < 0 ? Math.ceil(-content.top) + 12 : 0);
        const width = content.right + nextX > canvas.width ? Math.ceil((content.right + nextX + 12) / 16) * 16 : canvas.width;
        const height = content.bottom + nextY > canvas.height ? Math.ceil((content.bottom + nextY + 12) / 16) * 16 : canvas.height;
        if (width === canvas.width && height === canvas.height && nextX === offsetX && nextY === offsetY) return;
        if (width > 16384 || height > 16384 || width * height > 33554432) {
            const error = new RangeError('图形范围过大，请重新开始并减小坐标或缩放参数。');
            error.code = 'CANVAS_RANGE';
            throw error;
        }
        offsetX = nextX; offsetY = nextY;
        // Grow the bitmap, never the algorithm's logical coordinates. Keep size stable during animation.
        canvas.width = Math.max(1000, width);
        canvas.height = Math.max(500, height);
        world.framenum = frame;
        drawScene();
        runtime.onCanvasSize({ width: canvas.width, height: canvas.height, offsetX, offsetY });
    };
    world.draw = draw;
    return () => {
        if (world.draw === draw) world.draw = originalDraw;
        restorers.reverse().forEach(restore => restore());
    };
}

export async function loadAlgorithm(definition) {
    const modules = await Promise.all(definition.scripts
        .filter(name => name !== 'Algorithm')
        .map(async name => dependencies[name] || (await algorithmLoaders[name]()).default));
    return function mount(host, { speed = 75, onState = () => {}, onError = () => {}, onCanvasSize = () => {} } = {}) {
        const ownerDocument = host.ownerDocument;
        const runtime = {
            speed, onState, onError, onCanvasSize, userText: new Set(),
            document: {
                getElementById: id => host.querySelector(`[id="${id}"]`),
                createElement: tag => {
                    const element = ownerDocument.createElement(tag);
                    if (tag.toLowerCase() === 'input') {
                        let descriptor;
                        for (let owner = element; owner && !descriptor; owner = Object.getPrototypeOf(owner)) descriptor = Object.getOwnPropertyDescriptor(owner, 'value');
                        let currentValue = element.value;
                        Object.defineProperty(element, 'value', {
                            configurable: true, enumerable: true,
                            get() {
                                const value = descriptor && descriptor.get ? descriptor.get.call(this) : currentValue;
                                if (String(this.type).toLowerCase() === 'text' && value) runtime.userText.add(String(value));
                                return value;
                            },
                            set(value) {
                                if (descriptor && descriptor.set) descriptor.set.call(this, value);
                                else currentValue = String(value);
                            }
                        });
                        const setAttribute = element.setAttribute;
                        element.setAttribute = function(name, value) {
                            const label = name.toLowerCase() === 'value' && String(this.type).toLowerCase() === 'button';
                            return setAttribute.call(this, name, label ? translateLabel(value) : value);
                        };
                    }
                    return element;
                },
                createTextNode: text => ownerDocument.createTextNode(translateLabel(text))
            }
        };
        try {
            baseInstallers.forEach(install => install(runtime));
            const cleanups = [];
            const dispose = runtime.dispose;
            runtime.dispose = () => { cleanups.splice(0).forEach(cleanup => cleanup()); dispose(); };
            const ObjectManager = runtime.ObjectManager;
            runtime.ObjectManager = function(...args) {
                const world = new ObjectManager(...args);
                cleanups.push(attachCanvasPresentation(runtime, world));
                return world;
            };
            runtime.ObjectManager.prototype = ObjectManager.prototype;
            modules.forEach(install => install(runtime));
            runtime.init(...definition.args);
            runtime.objectManager.draw();
            runtime.publishState();
            return runtime;
        } catch (error) {
            if (runtime.dispose) runtime.dispose();
            throw error;
        }
    };
}
