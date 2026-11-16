<template>
    <main class="algorithm-page">
        <div class="algorithm-container">
            <button type="button" class="back-link" aria-label="返回上一页" @click="$router.back()"><i class="el-icon-arrow-left" /> 返回</button>
            <header class="algorithm-header">
                <span class="eyebrow">ALGORITHM VISUALIZATION</span>
                <h1>算法演示</h1>
                <p>从数据结构到算法，让每一步变化都清晰可见。</p>
            </header>
            <div class="catalog-tools">
                <el-input v-model="query" placeholder="搜索算法名称，如排序、AVL、Dijkstra" prefix-icon="el-icon-search" clearable aria-label="搜索算法" />
                <div class="category-filters" aria-label="算法分类">
                    <button :class="{ active: category === '' }" @click="category = ''">全部分类</button>
                    <button v-for="group in algorithmGroups" :key="group.id" :class="{ active: category === group.id }" @click="category = group.id">{{ group.name }}</button>
                </div>
            </div>
            <section v-for="group in visibleGroups" :key="group.id" class="algorithm-group">
                <h2><i :class="group.icon" aria-hidden="true" /> {{ group.name }}</h2>
                <div class="algorithm-grid">
                    <router-link v-for="item in group.items" :key="item.id" :to="{ name: 'AlgorithmDemo', params: { algorithm: item.id } }" class="algorithm-card">
                        <span class="algorithm-card-icon"><i :class="group.icon" aria-hidden="true" /></span>
                        <span class="algorithm-card-label"><strong>{{ item.name }}</strong><small>{{ item.englishName }}</small></span>
                        <i class="el-icon-arrow-right" aria-hidden="true" />
                    </router-link>
                </div>
            </section>
            <div v-if="!visibleGroups.length" class="empty-state">没有找到匹配的算法，请尝试其他关键词。</div>
            <p class="source-credit">原始算法与动画：<a href="https://www.cs.usfca.edu/~galles/visualization/Algorithms.html" target="_blank" rel="noopener noreferrer">David Galles · University of San Francisco</a> · <a :href="licenseUrl" target="_blank" rel="noopener noreferrer">开源许可</a></p>
        </div>
    </main>
</template>

<script>
import './algorithm.css';
import { algorithmGroups } from './catalog';

const catalogState = { query: '', category: '' };

export default {
    name: 'AlgorithmIndex',
    data: () => ({ algorithmGroups, ...catalogState, licenseUrl: process.env.BASE_URL + 'licenses/algorithm-visualization.txt' }),
    beforeRouteLeave(to, from, next) {
        Object.assign(catalogState, { query: this.query, category: this.category });
        next();
    },
    computed: {
        visibleGroups() {
            const keyword = this.query.trim().toLowerCase();
            return this.algorithmGroups.filter(group => !this.category || group.id === this.category)
                .map(group => ({ ...group, items: group.items.filter(item =>
                    `${item.name} ${item.englishName} ${item.id} ${group.name} ${(item.aliases || []).join(' ')}`.toLowerCase().includes(keyword)) }))
                .filter(group => group.items.length);
        }
    }
};
</script>

