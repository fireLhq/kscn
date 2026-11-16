export function downloadBlob(blob, name) {
    const url = URL.createObjectURL(blob), anchor = document.createElement('a');
    anchor.href = url; anchor.download = name; document.body.appendChild(anchor); anchor.click(); anchor.remove();
    setTimeout(() => URL.revokeObjectURL(url), 30000);
}
export function svgBlob(svg) { return new Blob([svg], { type: 'image/svg+xml;charset=utf-8' }); }
export async function svgPng(svg, scale = 2) {
    if (![1,2,3,4].includes(scale)) throw new Error('PNG 倍率须在 1–4 之间');
    const url = URL.createObjectURL(svgBlob(svg));
    try {
        const image = new Image(); image.src = url; await image.decode();
        const width = Math.ceil(image.naturalWidth * scale), height = Math.ceil(image.naturalHeight * scale);
        if (width > 12000 || height > 12000 || width * height > 24000000) throw new Error('PNG 尺寸过大，请减小倍率或图像尺寸');
        const canvas = document.createElement('canvas'); canvas.width = width; canvas.height = height;
        const context = canvas.getContext('2d'); if (!context) throw new Error('浏览器不支持图像导出');
        context.drawImage(image,0,0,width,height);
        const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png'));
        if (!blob) throw new Error('PNG 导出失败'); return blob;
    } finally { URL.revokeObjectURL(url); }
}
