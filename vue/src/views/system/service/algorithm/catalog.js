export const algorithmGroups = [
    {
        "id": "group-0",
        "name": "线性结构与基础堆",
        "icon": "el-icon-files",
        "items": [
            {
                "id": "StackArray",
                "name": "栈（数组）",
                "scripts": [
                    "Algorithm",
                    "StackArray"
                ],
                "args": [],
                "englishName": "Stack (Array)"
            },
            {
                "id": "StackLL",
                "name": "栈（链表）",
                "scripts": [
                    "Algorithm",
                    "StackLL"
                ],
                "args": [],
                "englishName": "Stack (Linked List)"
            },
            {
                "id": "QueueArray",
                "name": "队列（数组）",
                "scripts": [
                    "Algorithm",
                    "QueueArray"
                ],
                "args": [],
                "englishName": "Queue (Array)"
            },
            {
                "id": "QueueLL",
                "name": "队列（链表）",
                "scripts": [
                    "Algorithm",
                    "QueueLL"
                ],
                "args": [],
                "englishName": "Queue (Linked List)"
            },
            {
                "id": "SimpleStack",
                "name": "简单栈",
                "scripts": [
                    "Algorithm",
                    "SimpleStack"
                ],
                "args": [],
                "englishName": "Simple Stack"
            },
            {
                "id": "Heap",
                "name": "堆",
                "scripts": [
                    "Algorithm",
                    "Heap"
                ],
                "args": [],
                "englishName": "Heap"
            }
        ]
    },
    {
        "id": "group-1",
        "name": "哈希结构",
        "icon": "el-icon-key",
        "items": [
            {
                "id": "ClosedHash",
                "name": "闭散列表（开放寻址）",
                "scripts": [
                    "Algorithm",
                    "Hash",
                    "ClosedHash"
                ],
                "args": [],
                "englishName": "Closed Hash Table (Open Addressing)",
                "aliases": [
                    "闭散列（开放寻址）"
                ]
            },
            {
                "id": "ClosedHashBucket",
                "name": "闭散列表（分桶）",
                "scripts": [
                    "Algorithm",
                    "Hash",
                    "ClosedHashBucket"
                ],
                "args": [],
                "englishName": "Closed Hash Table (Buckets)",
                "aliases": [
                    "闭散列（分桶）"
                ]
            },
            {
                "id": "OpenHash",
                "name": "开散列表（链地址）",
                "scripts": [
                    "Algorithm",
                    "Hash",
                    "OpenHash"
                ],
                "args": [],
                "englishName": "Open Hash Table (Closed Addressing)",
                "aliases": [
                    "开散列（链地址）"
                ]
            }
        ]
    },
    {
        "id": "group-2",
        "name": "查找与树结构",
        "icon": "el-icon-share",
        "items": [
            {
                "id": "Search",
                "name": "二分与线性查找（有序列表）",
                "scripts": [
                    "Algorithm",
                    "Search"
                ],
                "args": [],
                "englishName": "Binary and Linear Search (Sorted List)",
                "aliases": [
                    "线性与二分查找",
                    "二分查找",
                    "线性查找"
                ]
            },
            {
                "id": "BST",
                "name": "二叉搜索树",
                "scripts": [
                    "Algorithm",
                    "BST"
                ],
                "args": [],
                "englishName": "Binary Search Tree",
                "aliases": [
                    "二叉搜索树（BST）"
                ]
            },
            {
                "id": "AVLTree",
                "name": "AVL树（平衡二叉搜索树）",
                "scripts": [
                    "Algorithm",
                    "AVL"
                ],
                "args": [],
                "englishName": "AVL Tree (Balanced Binary Search Tree)",
                "aliases": [
                    "平衡二叉搜索树（AVL）"
                ]
            },
            {
                "id": "RedBlack",
                "name": "红黑树",
                "scripts": [
                    "Algorithm",
                    "RedBlack"
                ],
                "args": [],
                "englishName": "Red-Black Tree"
            },
            {
                "id": "SplayTree",
                "name": "伸展树",
                "scripts": [
                    "Algorithm",
                    "SplayTree"
                ],
                "args": [],
                "englishName": "Splay Tree",
                "aliases": [
                    "伸展树（Splay）"
                ]
            },
            {
                "id": "BTree",
                "name": "B树",
                "scripts": [
                    "Algorithm",
                    "BTree"
                ],
                "args": [],
                "englishName": "B Tree"
            },
            {
                "id": "BPlusTree",
                "name": "B+树",
                "scripts": [
                    "Algorithm",
                    "BPlusTree"
                ],
                "args": [],
                "englishName": "B+ Tree"
            },
            {
                "id": "Trie",
                "name": "字典树（前缀树）",
                "aliases": [
                    "前缀树",
                    "单词查找树",
                    "字典树（Trie）"
                ],
                "scripts": [
                    "Algorithm",
                    "Trie"
                ],
                "args": [],
                "englishName": "Trie (Prefix Tree)"
            },
            {
                "id": "RadixTree",
                "name": "基数树（压缩字典树）",
                "aliases": [
                    "压缩字典树",
                    "压缩前缀树",
                    "Compact Trie",
                    "基数树（Radix）"
                ],
                "scripts": [
                    "Algorithm",
                    "RadixTree"
                ],
                "args": [],
                "englishName": "Radix Tree (Compact Trie)"
            },
            {
                "id": "TST",
                "name": "三向搜索树",
                "aliases": [
                    "三向单词查找树",
                    "三叉搜索树",
                    "Ternary Search Tree",
                    "三向搜索树（TST）"
                ],
                "scripts": [
                    "Algorithm",
                    "TST"
                ],
                "args": [],
                "englishName": "Ternary Search Tree"
            }
        ]
    },
    {
        "id": "group-3",
        "name": "排序",
        "icon": "el-icon-sort",
        "items": [
            {
                "id": "ComparisonSort",
                "name": "比较排序",
                "scripts": [
                    "Algorithm",
                    "ComparisonSort"
                ],
                "args": [],
                "englishName": "Comparison Sorting"
            },
            {
                "id": "BucketSort",
                "name": "桶排序",
                "scripts": [
                    "Algorithm",
                    "BucketSort"
                ],
                "args": [],
                "englishName": "Bucket Sort"
            },
            {
                "id": "CountingSort",
                "name": "计数排序",
                "scripts": [
                    "Algorithm",
                    "CountingSort"
                ],
                "args": [],
                "englishName": "Counting Sort"
            },
            {
                "id": "RadixSort",
                "name": "基数排序",
                "scripts": [
                    "Algorithm",
                    "RadixSort"
                ],
                "args": [],
                "englishName": "Radix Sort"
            },
            {
                "id": "HeapSort",
                "name": "堆排序",
                "scripts": [
                    "Algorithm",
                    "HeapSort"
                ],
                "args": [],
                "englishName": "Heap Sort"
            }
        ]
    },
    {
        "id": "group-4",
        "name": "高级堆结构",
        "icon": "el-icon-collection",
        "items": [
            {
                "id": "BinomialQueue",
                "name": "二项式队列",
                "scripts": [
                    "Algorithm",
                    "BinomialQueue"
                ],
                "args": [],
                "englishName": "Binomial Queue"
            },
            {
                "id": "FibonacciHeap",
                "name": "斐波那契堆",
                "scripts": [
                    "Algorithm",
                    "FibonacciHeap"
                ],
                "args": [],
                "englishName": "Fibonacci Heap"
            },
            {
                "id": "LeftistHeap",
                "name": "左偏堆",
                "scripts": [
                    "Algorithm",
                    "LeftistHeap"
                ],
                "args": [],
                "englishName": "Leftist Heap"
            },
            {
                "id": "SkewHeap",
                "name": "斜堆",
                "scripts": [
                    "Algorithm",
                    "SkewHeap"
                ],
                "args": [],
                "englishName": "Skew Heap"
            }
        ]
    },
    {
        "id": "group-5",
        "name": "图论",
        "icon": "el-icon-connection",
        "items": [
            {
                "id": "BFS",
                "name": "广度优先搜索",
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "BFS"
                ],
                "args": [],
                "englishName": "Breadth-First Search",
                "aliases": [
                    "广度优先搜索（BFS）"
                ]
            },
            {
                "id": "DFS",
                "name": "深度优先搜索",
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "DFS"
                ],
                "args": [],
                "englishName": "Depth-First Search",
                "aliases": [
                    "深度优先搜索（DFS）"
                ]
            },
            {
                "id": "Dijkstra",
                "name": "Dijkstra 最短路径",
                "aliases": [
                    "迪杰斯特拉",
                    "戴克斯特拉",
                    "Dijkstra 最短路径算法"
                ],
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "DijkstraPrim"
                ],
                "args": [
                    true
                ],
                "englishName": "Dijkstra Shortest Path"
            },
            {
                "id": "Prim",
                "name": "Prim 最小生成树",
                "aliases": [
                    "普里姆",
                    "普利姆",
                    "Prim 最小生成树算法"
                ],
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "DijkstraPrim"
                ],
                "args": [
                    false
                ],
                "englishName": "Prim Minimum Cost Spanning Tree"
            },
            {
                "id": "Kruskal",
                "name": "Kruskal 最小生成树",
                "aliases": [
                    "克鲁斯卡尔",
                    "Kruskal 最小生成树算法"
                ],
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "Kruskal"
                ],
                "args": [
                    false
                ],
                "englishName": "Kruskal Minimum Cost Spanning Tree"
            },
            {
                "id": "Floyd",
                "name": "Floyd-Warshall 最短路径（全源）",
                "aliases": [
                    "弗洛伊德",
                    "弗洛德",
                    "Floyd-Warshall",
                    "Floyd 全源最短路径算法"
                ],
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "Floyd"
                ],
                "args": [],
                "englishName": "Floyd-Warshall Shortest Paths (All Pairs)"
            },
            {
                "id": "ConnectedComponent",
                "name": "连通分量",
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "ConnectedComponent"
                ],
                "args": [],
                "englishName": "Connected Components"
            },
            {
                "id": "DisjointSets",
                "name": "并查集",
                "scripts": [
                    "Algorithm",
                    "DisjointSet"
                ],
                "args": [],
                "englishName": "Disjoint Sets"
            },
            {
                "id": "TopoSortDFS",
                "name": "拓扑排序（深度优先搜索）",
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "TopoSortDFS"
                ],
                "args": [],
                "englishName": "Topological Sort (Depth-First Search)"
            },
            {
                "id": "TopoSortIndegree",
                "name": "拓扑排序（入度数组）",
                "scripts": [
                    "Algorithm",
                    "Graph",
                    "TopoSortIndegree"
                ],
                "args": [],
                "englishName": "Topological Sort (Indegree Array)",
                "aliases": [
                    "拓扑排序（入度法）"
                ]
            }
        ]
    },
    {
        "id": "group-6",
        "name": "动态规划",
        "icon": "el-icon-data-analysis",
        "items": [
            {
                "id": "DPFib",
                "name": "斐波那契数（第 n 项）",
                "scripts": [
                    "Algorithm",
                    "DPFib"
                ],
                "args": [],
                "englishName": "Fibonacci Number (Nth Term)",
                "aliases": [
                    "斐波那契数（动态规划）"
                ]
            },
            {
                "id": "DPChange",
                "name": "找零",
                "scripts": [
                    "Algorithm",
                    "DPChange"
                ],
                "args": [],
                "englishName": "Making Change",
                "aliases": [
                    "找零问题（动态规划）"
                ]
            },
            {
                "id": "DPLCS",
                "name": "最长公共子序列",
                "aliases": [
                    "LCS",
                    "Longest Common Subsequence",
                    "最长公共子序列（动态规划）"
                ],
                "scripts": [
                    "Algorithm",
                    "DPLCS"
                ],
                "args": [],
                "englishName": "Longest Common Subsequence"
            }
        ]
    },
    {
        "id": "group-7",
        "name": "递归",
        "icon": "el-icon-refresh-left",
        "items": [
            {
                "id": "RecFact",
                "name": "阶乘",
                "scripts": [
                    "Algorithm",
                    "Recursive",
                    "RecFact"
                ],
                "args": [],
                "englishName": "Factorial",
                "aliases": [
                    "阶乘（递归）"
                ]
            },
            {
                "id": "RecQueens",
                "name": "N 皇后",
                "scripts": [
                    "Algorithm",
                    "Recursive",
                    "RecQueens"
                ],
                "args": [],
                "englishName": "N-Queens",
                "aliases": [
                    "N 皇后问题"
                ]
            },
            {
                "id": "RecReverse",
                "name": "字符串反转",
                "scripts": [
                    "Algorithm",
                    "Recursive",
                    "RecReverse"
                ],
                "args": [],
                "englishName": "String Reversal",
                "aliases": [
                    "字符串反转（递归）"
                ]
            }
        ]
    },
    {
        "id": "group-8",
        "name": "计算机图形学",
        "icon": "el-icon-coordinate",
        "items": [
            {
                "id": "ChangingCoordinates2D",
                "name": "坐标系变换（二维）",
                "scripts": [
                    "Algorithm",
                    "ChangingCoordinate2D"
                ],
                "args": [],
                "englishName": "Coordinate System Transformation (2D)",
                "aliases": [
                    "2D 坐标变换"
                ]
            },
            {
                "id": "ChangingCoordinates3D",
                "name": "坐标系变换（三维）",
                "scripts": [
                    "Algorithm",
                    "ChangingCoordinate3D"
                ],
                "args": [],
                "englishName": "Coordinate System Transformation (3D)",
                "aliases": [
                    "3D 坐标变换"
                ]
            },
            {
                "id": "RotateScale2D",
                "name": "旋转与缩放矩阵（二维）",
                "scripts": [
                    "Algorithm",
                    "RotateScale2D"
                ],
                "args": [],
                "englishName": "Rotation and Scale Matrices (2D)",
                "aliases": [
                    "2D 旋转缩放"
                ]
            },
            {
                "id": "RotateScale3D",
                "name": "旋转与缩放矩阵（三维）",
                "scripts": [
                    "Algorithm",
                    "RotateScale3D"
                ],
                "args": [],
                "englishName": "Rotation and Scale Matrices (3D)",
                "aliases": [
                    "3D 旋转缩放"
                ]
            },
            {
                "id": "RotateTranslate2D",
                "name": "旋转与平移矩阵（二维）",
                "scripts": [
                    "Algorithm",
                    "RotateTranslate2D"
                ],
                "args": [],
                "englishName": "Rotation and Translation Matrices (2D)",
                "aliases": [
                    "2D 旋转变换"
                ]
            }
        ]
    }
];

export const algorithms = algorithmGroups.flatMap(group => group.items.map(item => ({ ...item, category: group.name, icon: group.icon })));
export function findAlgorithm(id) { return algorithms.find(item => item.id === id); }
