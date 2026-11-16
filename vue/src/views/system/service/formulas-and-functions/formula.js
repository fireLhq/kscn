import { mathjax } from 'mathjax-full/js/mathjax.js';
import { TeX } from 'mathjax-full/js/input/tex.js';
import { SVG } from 'mathjax-full/js/output/svg.js';
import { liteAdaptor } from 'mathjax-full/js/adaptors/liteAdaptor.js';
import { RegisterHTMLHandler } from 'mathjax-full/js/handlers/html.js';
import 'mathjax-full/js/input/tex/ams/AmsConfiguration.js';

const adaptor = liteAdaptor();
RegisterHTMLHandler(adaptor);
const document = mathjax.document('', {
    InputJax: new TeX({ packages: ['base', 'ams'], maxBuffer: 10000, maxMacros: 1000,
        formatError: (_, error) => { throw new Error(error.message || 'LaTeX 语法不正确'); } }),
    OutputJax: new SVG({ fontCache: 'none' })
});
export function normalizeLatex(text) {
    let source = text.trim();
    if (source.startsWith('$$') && source.endsWith('$$') && source.length >= 4) source = source.slice(2,-2).trim();
    else if (source.startsWith('$') && source.endsWith('$') && source.length >= 2) source = source.slice(1,-1).trim();
    else if ((source.startsWith('\\[') && source.endsWith('\\]')) || (source.startsWith('\\(') && source.endsWith('\\)'))) source = source.slice(2,-2).trim();
    if (!source || source.length > 4000) throw new Error('公式不能为空，且最多 4000 个字符');
    // Only math packages are enabled. Reject commands which could insert resources or persistent macros.
    if (/\\(?:href|url|includegraphics|html\w*|style|class|cssId|require|def|gdef|let|newcommand|renewcommand)\b/.test(source)) throw new Error('仅支持数学排版命令，不支持链接、资源或自定义宏');
    return source;
}
export function formulaSvg(text, { fontSize = 32, color = '#223248', padding = 12 } = {}) {
    const latex = normalizeLatex(text);
    if (!Number.isFinite(fontSize) || fontSize < 12 || fontSize > 120 || !/^#[\da-f]{6}$/i.test(color)) throw new Error('公式字号或颜色不正确');
    const container = document.convert(latex, { display: true });
    const node = adaptor.firstChild(container);
    const box = adaptor.getAttribute(node, 'viewBox').split(/\s+/).map(Number);
    if (box.length !== 4 || box.some(n => !Number.isFinite(n))) throw new Error('公式渲染失败');
    const pad = padding / fontSize * 1000;
    const [x,y,w,h] = box;
    const width = Math.ceil(w / 1000 * fontSize + padding * 2), height = Math.ceil(h / 1000 * fontSize + padding * 2);
    if (width > 12000 || height > 12000 || width * height > 16000000) throw new Error('公式图像过大，请缩小字号或拆分公式');
    const body = adaptor.innerHTML(node);
    return { latex, width, height, svg: `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="${x-pad} ${y-pad} ${w+2*pad} ${h+2*pad}" color="${color}" role="img"><title>数学公式</title>${body}</svg>` };
}
