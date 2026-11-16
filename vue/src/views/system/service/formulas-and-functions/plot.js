import { compileCurve } from './expression';

export const defaultPlot = () => ({ xMin: -6, xMax: 6, yMin: -4, yMax: 4, axisXMin: -5, axisXMax: 5, axisYMin: -3, axisYMax: 3,
    xStep: 1, yStep: 1, decimals: 1, samples: 800, gridSize: 100, width: 960, height: 640,
    axes: true, curves: true, expressions: true, ticks: true, numbers: true, grid: false,
    extend: true, extension: 0.35, arrows: true, transparent: true, equalScale: true });
export const escapeXml = value => String(value).replace(/[&<>"']/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&apos;' }[char]));
export function validateRange(min, max, label) {
    if (!Number.isFinite(min) || !Number.isFinite(max) || min >= max || Math.abs(min) > 1e6 || Math.abs(max) > 1e6 || max - min < 1e-6) throw new Error(`${label}须满足最小值 < 最大值，绝对值不超过 100 万，跨度不少于 0.000001`);
}
export function tickValues(min, max, step) {
    if (!Number.isFinite(step) || step <= 0) throw new Error('刻度间隔必须大于 0');
    const first = Math.ceil(min / step - 1e-9), last = Math.floor(max / step + 1e-9);
    if (last - first > 200) throw new Error('单轴刻度不能超过 200 个，请增大刻度间隔');
    return Array.from({length: Math.max(0,last-first+1)}, (_,i) => (first+i)*step);
}
function validateOptions(o) {
    validateRange(o.xMin,o.xMax,'视窗 X 范围'); validateRange(o.yMin,o.yMax,'视窗 Y 范围');
    validateRange(o.axisXMin,o.axisXMax,'X 轴范围'); validateRange(o.axisYMin,o.axisYMax,'Y 轴范围');
    for (const [key,min,max] of [['width',320,2400],['height',240,1800],['samples',100,2400],['gridSize',30,220],['decimals',0,8]]) {
        if (!Number.isInteger(o[key]) || o[key] < min || o[key] > max) throw new Error(`${key} 超出允许范围`);
    }
    if (!Number.isFinite(o.extension) || o.extension < 0 || o.extension > 1) throw new Error('轴端延长比例须为 0–1');
}
const finite = Number.isFinite;
const number = value => Number(value.toFixed(3));
function point(x,y) { return `${number(x)},${number(y)}`; }
// Do not join explicit samples across poles or large jumps.
export function explicitPath(fn, mode, bounds, map, samples) {
    const from = mode === 'x' ? bounds.yMin : bounds.xMin, to = mode === 'x' ? bounds.yMax : bounds.xMax;
    let path = '', previous = null, segments = 0;
    const sample = t => { const value = mode === 'x' ? fn(0,t) : fn(t,0); return mode === 'x' ? [value,t] : [t,value]; };
    for (let i=0;i<=samples;i++) {
        const t = from + (to-from)*i/samples, [x,y] = sample(t), px = map.x(x), py = map.y(y);
        if (!finite(px) || !finite(py) || Math.abs(px) > 1e7 || Math.abs(py) > 1e7) { previous=null; continue; }
        let connect = false;
        if (previous) {
            const [mx,my] = sample((previous.t+t)/2), mpx=map.x(mx), mpy=map.y(my);
            const jump = Math.hypot(px-previous.x,py-previous.y);
            connect = finite(mpx) && finite(mpy) && Math.hypot(mpx-(px+previous.x)/2,mpy-(py+previous.y)/2) < Math.max(6,jump*0.4);
        }
        if (connect) { path += `L${point(px,py)}`; segments++; }
        else path += `M${point(px,py)}`;
        previous={t,x:px,y:py};
    }
    return {path,segments};
}
function crossing(fn, a, b) {
    if (!finite(a.v) || !finite(b.v) || (a.v >= 0) === (b.v >= 0)) return null;
    const scale = Math.max(Math.abs(a.v),Math.abs(b.v)); let left=a, right=b;
    for (let i=0;i<12;i++) {
        const x=(left.x+right.x)/2,y=(left.y+right.y)/2,v=fn(x,y);
        if (!finite(v)) return null;
        const mid={x,y,v};
        if ((v>=0) === (left.v>=0)) left=mid; else right=mid;
    }
    const t = Math.abs(left.v)/(Math.abs(left.v)+Math.abs(right.v));
    const x=left.x+(right.x-left.x)*t,y=left.y+(right.y-left.y)*t;
    // A sign change at a pole is not a zero of the equation.
    if (!finite(fn(x,y)) || Math.abs(fn(x,y)) > scale * 0.1 + 1e-9) return null;
    return {x,y};
}
export function implicitPath(fn, bounds, map, resolution) {
    const dx=(bounds.xMax-bounds.xMin)/resolution,dy=(bounds.yMax-bounds.yMin)/resolution;
    let previous = Array.from({length:resolution+1},(_,i) => fn(bounds.xMin+i*dx,bounds.yMin));
    let path='', segments=0;
    for (let j=0;j<resolution;j++) {
        const y=bounds.yMin+j*dy, next = Array.from({length:resolution+1},(_,i) => fn(bounds.xMin+i*dx,y+dy));
        for (let i=0;i<resolution;i++) {
            const x=bounds.xMin+i*dx;
            const corners=[{x,y,v:previous[i]},{x:x+dx,y,v:previous[i+1]},{x:x+dx,y:y+dy,v:next[i+1]},{x,y:y+dy,v:next[i]}];
            const hits=[];
            for (let edge=0;edge<4;edge++) { const hit=crossing(fn,corners[edge],corners[(edge+1)%4]); if (hit) hits.push({edge,...hit}); }
            let pairs=[];
            if (hits.length===2) pairs=[[hits[0],hits[1]]];
            else if (hits.length===4) {
                const center=fn(x+dx/2,y+dy/2);
                pairs = (center>=0)===(corners[0].v>=0) ? [[hits[0],hits[1]],[hits[2],hits[3]]] : [[hits[0],hits[3]],[hits[1],hits[2]]];
            }
            for (const [a,b] of pairs) { path+=`M${point(map.x(a.x),map.y(a.y))}L${point(map.x(b.x),map.y(b.y))}`; segments++; }
        }
        previous=next;
    }
    return {path,segments};
}
export async function buildPlot(rows, options, crop = null) {
    const o={...options,...(crop || {})}; validateOptions(o);
    const active=rows.filter(row=>row.visible);
    if (rows.length>12) throw new Error('最多同时绘制 12 条表达式');
    const xticks=tickValues(Math.max(o.axisXMin,o.xMin),Math.min(o.axisXMax,o.xMax),o.xStep);
    const yticks=tickValues(Math.max(o.axisYMin,o.yMin),Math.min(o.axisYMax,o.yMax),o.yStep);
    const legendColumns=Math.max(18,Math.floor((o.width-145)/8));
    const legends=active.map(row=>{
        const value=row.mode==='implicit'||row.expression.includes('=')?row.expression:`${row.mode} = ${row.expression}`;
        return {row,lines:value.match(new RegExp(`.{1,${legendColumns}}`,'g'))||['']};
    });
    const legendHeight=o.expressions ? legends.reduce((sum,item)=>sum+item.lines.length*22,0)+12 : 0;
    const availableWidth=o.width-100, availableHeight=o.height-90-legendHeight;
    if (availableHeight<80) throw new Error('画布高度不足以容纳表达式，请增大高度或关闭表达式显示');
    let w=availableWidth,h=availableHeight;
    if (o.equalScale) { const scale=Math.min(w/(o.xMax-o.xMin),h/(o.yMax-o.yMin)); w=scale*(o.xMax-o.xMin);h=scale*(o.yMax-o.yMin); }
    const left=60+(availableWidth-w)/2,top=30+(availableHeight-h)/2;
    const map={x:x=>left+(x-o.xMin)/(o.xMax-o.xMin)*w,y:y=>top+h-(y-o.yMin)/(o.yMax-o.yMin)*h,height:h};
    const definitions=[`<clipPath id="view"><rect x="${left}" y="${top}" width="${w}" height="${h}"/></clipPath>`, '<marker id="arrow" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto" markerUnits="strokeWidth"><path d="M0,0 L6,3 L0,6" fill="none" stroke="#52657c" stroke-width="1.2"/></marker>'];
    const elements=[],paths=[],warnings=[];
    const line=(x1,y1,x2,y2,extra='') => `<line x1="${number(x1)}" y1="${number(y1)}" x2="${number(x2)}" y2="${number(y2)}" ${extra}/>`;
    const text=(x,y,value,extra='') => `<text x="${number(x)}" y="${number(y)}" ${extra}>${escapeXml(value)}</text>`;
    if (!o.transparent) elements.push(`<rect width="${o.width}" height="${o.height}" fill="#fff"/>`);
    if (o.grid) {
        const lines=[...xticks.map(x=>line(map.x(x),top,map.x(x),top+h)),...yticks.map(y=>line(left,map.y(y),left+w,map.y(y)))];
        elements.push(`<g stroke="#e4e8f0" stroke-width="1" clip-path="url(#view)">${lines.join('')}</g>`);
    }
    if (o.curves) for (let index=0;index<active.length;index++) {
        const row=active[index];
        validateRange(row.xMin,row.xMax,`第 ${index+1} 条曲线定义域`); validateRange(row.yMin,row.yMax,`第 ${index+1} 条曲线值域`);
        if (!/^#[\da-f]{6}$/i.test(row.color) || !finite(row.width) || row.width<0.5 || row.width>8) throw new Error('曲线颜色或线宽不正确');
        let fn;
        try { fn=compileCurve(row.expression,row.mode); } catch(e) { throw new Error(`第 ${index+1} 条：${e.message}`); }
        const bounds={xMin:Math.max(o.xMin,row.xMin),xMax:Math.min(o.xMax,row.xMax),yMin:Math.max(o.yMin,row.yMin),yMax:Math.min(o.yMax,row.yMax)};
        if (bounds.xMin>=bounds.xMax || bounds.yMin>=bounds.yMax) { warnings.push(`第 ${index+1} 条曲线与当前视窗无交集`); continue; }
        const result=row.mode==='implicit' ? implicitPath(fn,bounds,map,o.gridSize) : explicitPath(fn,row.mode,bounds,map,o.samples);
        const clip=`curve-${index}`;
        definitions.push(`<clipPath id="${clip}"><rect x="${map.x(bounds.xMin)}" y="${map.y(bounds.yMax)}" width="${map.x(bounds.xMax)-map.x(bounds.xMin)}" height="${map.y(bounds.yMin)-map.y(bounds.yMax)}"/></clipPath>`);
        paths.push(`<path data-curve="${index}" d="${result.path}" fill="none" stroke="${row.color}" stroke-width="${row.width}" stroke-linejoin="round" stroke-linecap="round" clip-path="url(#${clip})"/>`);
        if (!result.segments) warnings.push(`第 ${index+1} 条曲线在当前范围内没有可绘制线段`);
        await new Promise(resolve=>setTimeout(resolve,0));
    }
    elements.push(...paths);
    if (o.axes) {
        const axisColor='#52657c', marker=o.arrows ? 'marker-end="url(#arrow)"' : '';
        const zeroX=map.x(0),zeroY=map.y(0),xVisible=o.yMin<=0&&o.yMax>=0,yVisible=o.xMin<=0&&o.xMax>=0;
        const xmin=Math.max(o.xMin,o.axisXMin-(o.extend?o.extension*o.xStep:0)),xmax=Math.min(o.xMax,o.axisXMax+(o.extend?o.extension*o.xStep:0));
        const ymin=Math.max(o.yMin,o.axisYMin-(o.extend?o.extension*o.yStep:0)),ymax=Math.min(o.yMax,o.axisYMax+(o.extend?o.extension*o.yStep:0));
        if (xVisible&&xmin<xmax) elements.push(line(map.x(xmin),zeroY,map.x(xmax),zeroY,`stroke="${axisColor}" stroke-width="1.4" ${marker}`),text(Math.min(o.width-14,map.x(xmax)+10),zeroY-8,'x'));
        if (yVisible&&ymin<ymax) elements.push(line(zeroX,map.y(ymin),zeroX,map.y(ymax),`stroke="${axisColor}" stroke-width="1.4" ${marker}`),text(zeroX+10,Math.max(14,map.y(ymax)-8),'y'));
        const label=n=>Number((Math.abs(n)<1e-12?0:n).toFixed(o.decimals)).toString();
        if(xVisible&&xmin<xmax) for(const x of xticks) {
            if(o.ticks) elements.push(line(map.x(x),zeroY,map.x(x),zeroY-5,`stroke="${axisColor}"`));
            if(o.numbers) elements.push(text(map.x(x)+(Math.abs(x)<1e-12?-9:0),zeroY+18,label(x),'text-anchor="middle"'));
        }
        if(yVisible&&ymin<ymax) for(const y of yticks) {
            if(o.ticks) elements.push(line(zeroX,map.y(y),zeroX+5,map.y(y),`stroke="${axisColor}"`));
            if(o.numbers&&Math.abs(y)>1e-12) elements.push(text(zeroX-9,map.y(y)+4,label(y),'text-anchor="end"'));
        }
    }
    if(o.expressions) {
        let y=o.height-legendHeight+20;
        for(const {row,lines} of legends) {
            if(!/^#[\da-f]{6}$/i.test(row.color)) throw new Error('表达式颜色不正确');
            elements.push(line(60,y-4,80,y-4,`stroke="${row.color}" stroke-width="2"`));
            for(const value of lines) { elements.push(text(90,y,value)); y+=22; }
        }
    }
    const svg=`<svg xmlns="http://www.w3.org/2000/svg" width="${o.width}" height="${o.height}" viewBox="0 0 ${o.width} ${o.height}" role="img"><title>函数与方程图像</title><defs>${definitions.join('')}</defs><g font-family="Arial, sans-serif" font-size="13" fill="#52657c">${elements.join('')}</g></svg>`;
    return {svg,warnings,width:o.width,height:o.height};
}
