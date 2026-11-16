<template>
    <main class="algorithm-page algorithm-demo-page">
        <div class="algorithm-container">
            <header ref="demoHeader" class="demo-header">
                <button type="button" class="back-link" aria-label="返回上一页" @click="$router.back()"><i class="el-icon-arrow-left" /> 返回</button>
                <div v-if="definition" class="demo-title"><h1>{{ definition.name }}</h1><span>{{ definition.category }}</span></div>
                <el-button v-if="definition" size="small" icon="el-icon-refresh" :disabled="loading" @click="start">重新开始</el-button>
            </header>
            <template v-if="definition">
                <el-alert v-if="error" class="error-alert" :title="error" type="error" :closable="false" show-icon />
                <section ref="stage" v-loading="loading" class="demo-panel" :aria-label="definition.name + '动画演示'">
                    <div ref="controlDock" class="control-dock">
                        <div class="algorithm-controls" :inert="!ready ? '' : null" title="输入参数后点击操作按钮；动画运行时部分操作暂不可用。"><table id="AlgorithmSpecificControls" ref="controls" aria-label="算法参数与操作" /></div>
                        <div class="playback-bar">
                            <div class="playback-buttons">
                                <el-button size="mini" title="回到开头" aria-label="回到开头" :disabled="!canGoBack" @click="control('skipBack')">开头</el-button>
                                <el-button size="mini" :disabled="!canGoBack" @click="control('stepBack')">上一步</el-button>
                                <el-button type="primary" size="mini" :disabled="!ready" @click="togglePause">{{ state.paused ? '继续' : '暂停' }}</el-button>
                                <el-button size="mini" :disabled="!ready || !state.waiting" @click="control('step')">下一步</el-button>
                                <el-button size="mini" title="跳到结尾" aria-label="跳到结尾" :disabled="!ready || !state.running" @click="control('skipForward')">结尾</el-button>
                            </div>
                            <div class="speed-control"><label for="algorithm-speed">速度</label><input id="algorithm-speed" v-model.number="speed" type="range" min="0" max="100" :disabled="!ready" @input="changeSpeed"><span>{{ speed }}</span></div>
                            <div class="canvas-settings"><span class="animation-state">{{ stateText }}</span><el-switch v-model="fitCanvas" active-text="适应窗口" aria-label="画布适应窗口" /></div>
                        </div>
                    </div>
                    <div ref="canvasViewport" class="canvas-viewport"><canvas id="canvas" ref="canvas" width="1000" height="500" :style="canvasStyle" :aria-label="definition.name + '动画画布'" /></div>
                </section>
                <p class="demo-hint">可先暂停，再操作并逐步演示。关闭“适应窗口”可查看原始尺寸；离开或重新开始会清空演示。</p>
                <p class="source-credit">原始算法与动画：<a href="https://www.cs.usfca.edu/~galles/visualization/Algorithms.html" target="_blank" rel="noopener noreferrer">David Galles · University of San Francisco</a> · <a :href="licenseUrl" target="_blank" rel="noopener noreferrer">开源许可</a></p>
            </template>
            <div v-else class="empty-state">未找到这个算法，请返回算法目录重新选择。</div>
        </div>
    </main>
</template>

<script>
import './algorithm.css';
import { findAlgorithm } from './catalog';
import { loadAlgorithm } from './engine';

export default {
    name: 'AlgorithmDemo',
    data: () => ({ loading: false, ready: false, error: '', speed: 75, fitCanvas: true, canvasDisplayWidth: 1000, canvasWidth: 1000, canvasHeight: 500, licenseUrl: process.env.BASE_URL + 'licenses/algorithm-visualization.txt',
        state: { running: false, waiting: false, paused: false, canUndo: false } }),
    computed: {
        definition() { return findAlgorithm(this.$route.params.algorithm); },
        canGoBack() { return this.ready && this.state.canUndo && (!this.state.running || this.state.paused || this.state.waiting); },
        canvasStyle() {
            const scale = this.fitCanvas ? this.canvasDisplayWidth / this.canvasWidth : 1;
            return { width: `${this.canvasWidth * scale}px`, height: `${this.canvasHeight * scale}px` };
        },
        stateText() {
            if (this.loading) return '正在加载算法…';
            if (this.error) return '演示已停止';
            if (this.state.waiting) return '等待下一步';
            if (this.state.paused) return '已暂停';
            return this.state.running ? '正在演示' : '就绪';
        }
    },
    watch: {
        '$route.params.algorithm'() { this.start(); },
        fitCanvas() { this.$nextTick(this.syncCanvasSize); }
    },
    created() { this._generation = 0; this._runtime = null; },
    mounted() {
        if (window.ResizeObserver) this._layoutObserver = new ResizeObserver(this.syncCanvasSize);
        window.addEventListener('resize', this.syncCanvasSize);
        this.start();
    },
    beforeDestroy() {
        this._generation++;
        if (this._layoutObserver) this._layoutObserver.disconnect();
        window.removeEventListener('resize', this.syncCanvasSize);
        this.release();
    },
    methods: {
        syncCanvasSize() {
            const viewport = this.$refs.canvasViewport;
            if (!viewport) return;
            // Keep the natural drawing at 1:1 whenever it fits the width, regardless of height.
            // A short window must not shrink the diagram just to fit unused canvas space.
            this.canvasDisplayWidth = Math.max(1, Math.floor(Math.min(this.canvasWidth, viewport.clientWidth)));
        },
        observeLayout() {
            if (this._layoutObserver) {
                this._layoutObserver.disconnect();
                if (this.$refs.canvasViewport) this._layoutObserver.observe(this.$refs.canvasViewport);
            }
            this.syncCanvasSize();
        },
        release() {
            if (this._runtime) this._runtime.dispose();
            this._runtime = null;
            this.ready = false;
            if (this.$refs.controls) this.$refs.controls.replaceChildren();
        },
        async start() {
            const generation = ++this._generation;
            this.release();
            this.error = '';
            this.state = { running: false, waiting: false, paused: false, canUndo: false };
            this.loading = !!this.definition;
            if (!this.definition) return;
            try {
                const mount = await loadAlgorithm(this.definition);
                await this.$nextTick();
                if (generation !== this._generation) return;
                this.$refs.controls.textContent = '';
                this.canvasWidth = 1000;
                this.canvasHeight = 500;
                this.$refs.canvas.width = 1000;
                this.$refs.canvas.height = 500;
                this._runtime = mount(this.$refs.stage, {
                    speed: this.speed,
                    onState: state => { if (generation === this._generation) this.state = state; },
                    onCanvasSize: size => {
                        if (generation !== this._generation) return;
                        this.canvasWidth = size.width;
                        this.canvasHeight = size.height;
                        this.syncCanvasSize();
                    },
                    onError: error => { if (generation === this._generation) this.showError(error); }
                });
                this.ready = true;
            } catch (error) {
                if (generation === this._generation) this.showError(error);
            } finally {
                if (generation === this._generation) {
                    this.loading = false;
                    this.$nextTick(() => { if (generation === this._generation) this.observeLayout(); });
                }
            }
        },
        showError(error) {
            console.error('算法演示运行失败', error);
            if (this._runtime) this._runtime.dispose();
            this.ready = false;
            this.error = error.code === 'CANVAS_RANGE' ? error.message : '演示运行失败，请点击“重新开始”重试。';
        },
        control(method) {
            if (!this.ready) return;
            try { this._runtime.animationManager[method](); this._runtime.publishState(); }
            catch (error) { this.showError(error); }
        },
        togglePause() {
            if (!this.ready) return;
            this._runtime.animationManager.SetPaused(!this.state.paused);
            this._runtime.publishState();
        },
        changeSpeed() { if (this._runtime) this._runtime.animationManager.SetSpeed(this.speed); }
    }
};
</script>

