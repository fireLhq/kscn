import MarkdownIt from 'markdown-it';

const markdown = new MarkdownIt({ html: false, linkify: false });
export function renderDocument(text) {
    return '<!doctype html><html><head><meta charset="utf-8"><meta http-equiv="Content-Security-Policy" content="default-src \'none\'; style-src \'unsafe-inline\'; img-src data:"><style>body{max-width:900px;margin:32px auto;padding:0 20px;font:16px/1.8 sans-serif;overflow-wrap:anywhere}pre{white-space:pre-wrap}table,td,th{border:1px solid #ddd;border-collapse:collapse;padding:6px}</style></head><body>' + markdown.render(text) + '</body></html>';
}
export function convertText(text, mode) {
    if (mode === 'html') return renderDocument(text);
    if (mode === 'json') return JSON.stringify(JSON.parse(text), null, 2);
    if (mode === 'base64') {
        const bytes = new TextEncoder().encode(text);
        let binary = '';
        for (const byte of bytes) binary += String.fromCharCode(byte);
        return btoa(binary);
    }
    if (mode === 'unbase64') return new TextDecoder('utf-8', { fatal: true }).decode(Uint8Array.from(atob(text.trim()), c => c.charCodeAt(0)));
    return text;
}
export function validName(name) {
    if (!name.trim() || /[\\/:*?"<>|\x00-\x1f]/.test(name) || /[. ]$/.test(name) || name === '.' || name === '..' || name.length > 180) throw new Error('名称不能为空、过长或包含非法字符');
    return name;
}
export function renameFiles(files, base, style, digits) {
    validName(base);
    const seen = new Set();
    return files.map((file, index) => {
        const ext = file.name.lastIndexOf('.') > 0 ? file.name.slice(file.name.lastIndexOf('.')) : '';
        const sequence = String(index + 1).padStart(Math.max(1, Math.min(8, digits)), '0');
        const suffix = style === 'paren' ? `(${sequence})` : (style === 'dash' ? '-' : style === 'underscore' ? '_' : '') + sequence;
        const name = validName(base + suffix + ext);
        if (seen.has(name.toLowerCase())) throw new Error('生成的文件名有重复，请调整命名方案');
        seen.add(name.toLowerCase());
        return { name, blob: file };
    });
}

// ZIP STORE: preserve file bytes and UTF-8 filenames without a network library.
const crcTable = new Uint32Array(256);
for (let n = 0; n < 256; n++) {
    let crc = n;
    for (let bit = 0; bit < 8; bit++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0);
    crcTable[n] = crc >>> 0;
}
export async function zipFiles(entries) {
    const chunks = [], central = []; let offset = 0, directorySize = 0;
    for (const entry of entries) {
        const data = new Uint8Array(await entry.blob.arrayBuffer());
        const name = new TextEncoder().encode(validName(entry.name));
        let crc = -1;
        for (let start = 0; start < data.length; start += 1048576) {
            const limit = Math.min(data.length, start + 1048576);
            for (let i = start; i < limit; i++) crc = (crc >>> 8) ^ crcTable[(crc ^ data[i]) & 255];
            await new Promise(resolve => setTimeout(resolve, 0));
        }
        crc = (crc ^ -1) >>> 0;
        const header = new Uint8Array(30 + name.length), h = new DataView(header.buffer);
        h.setUint32(0, 0x04034b50, true); h.setUint16(4, 20, true); h.setUint16(6, 0x800, true);
        h.setUint16(12, 0x21, true);
        h.setUint32(14, crc, true); h.setUint32(18, data.length, true); h.setUint32(22, data.length, true);
        h.setUint16(26, name.length, true); header.set(name, 30);
        const record = new Uint8Array(46 + name.length), c = new DataView(record.buffer);
        c.setUint32(0, 0x02014b50, true); c.setUint16(4, 20, true); c.setUint16(6, 20, true); c.setUint16(8, 0x800, true);
        c.setUint16(14, 0x21, true);
        c.setUint32(16, crc, true); c.setUint32(20, data.length, true); c.setUint32(24, data.length, true);
        c.setUint16(28, name.length, true); c.setUint32(42, offset, true); record.set(name, 46);
        chunks.push(header, data); central.push(record); offset += header.length + data.length; directorySize += record.length;
    }
    const end = new Uint8Array(22), e = new DataView(end.buffer);
    e.setUint32(0, 0x06054b50, true); e.setUint16(8, entries.length, true); e.setUint16(10, entries.length, true);
    e.setUint32(12, directorySize, true); e.setUint32(16, offset, true);
    return new Blob([...chunks, ...central, end], { type: 'application/zip' });
}
export function downloadResult(result) {
    const url = URL.createObjectURL(result.blob), link = document.createElement('a');
    link.href = url; link.download = result.name; document.body.appendChild(link); link.click(); link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 60000);
}
export async function processImage(file, { width, height, rotation = 0, format = 'image/jpeg', quality = 0.8 }) {
    const url = URL.createObjectURL(file);
    try {
        const image = new Image(); image.src = url; await image.decode();
        const w = Math.round(width || image.naturalWidth), h = Math.round(height || image.naturalHeight);
        if (w < 1 || h < 1 || w > 12000 || h > 12000 || w * h > 24000000) throw new Error('图片尺寸须在 1–12000 像素内，且总像素不超过 2400 万');
        const canvas = document.createElement('canvas'), sideways = rotation % 180 !== 0;
        canvas.width = sideways ? h : w; canvas.height = sideways ? w : h;
        const ctx = canvas.getContext('2d');
        if (format === 'image/jpeg') { ctx.fillStyle = '#fff'; ctx.fillRect(0, 0, canvas.width, canvas.height); }
        ctx.translate(canvas.width / 2, canvas.height / 2); ctx.rotate(rotation * Math.PI / 180); ctx.drawImage(image, -w / 2, -h / 2, w, h);
        const blob = await new Promise(resolve => canvas.toBlob(resolve, format, quality));
        if (!blob || blob.type !== format) throw new Error('浏览器不支持该图片输出格式，请更换格式');
        return blob;
    } finally { URL.revokeObjectURL(url); }
}
