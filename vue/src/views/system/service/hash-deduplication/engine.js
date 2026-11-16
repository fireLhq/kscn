import SparkMD5 from 'spark-md5';

export const naturalCompare = (a, b) => a.localeCompare(b, 'zh-CN', { numeric: true, sensitivity: 'base' });
export function filterFiles(files, recursive, extensions) {
    const exts = new Set(extensions.toLowerCase().split(/[,;，；\s]+/).filter(Boolean).map(e => e.replace(/^\*?\.?/, '')));
    return files.filter(file => {
        const path = file.webkitRelativePath || file.name;
        const ext = file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : '';
        return (recursive || path.split('/').length <= 2) && (!exts.size || exts.has(ext));
    }).sort((a, b) => naturalCompare(a.webkitRelativePath || a.name, b.webkitRelativePath || b.name));
}
export function duplicateGroups(rows) {
    const map = new Map();
    for (const row of rows) {
        if (!row.hash) continue;
        const key = row.file.size + ':' + row.hash;
        if (!map.has(key)) map.set(key, []);
        map.get(key).push(row);
    }
    return [...map.entries()].filter(([, files]) => files.length > 1)
        .map(([key, files]) => ({ key, hash: files[0].hash, files }))
        .sort((a, b) => b.files.length - a.files.length || naturalCompare(a.key, b.key));
}
export function chooseKeep(files, rule, keyword, tie = 'path') {
    let candidates = [...files];
    if (rule === 'keyword' || rule === 'exclude') {
        const value = keyword.trim().toLowerCase();
        if (!value) return null;
        candidates = candidates.filter(r => r.path.toLowerCase().includes(value) === (rule === 'keyword'));
    }
    if (!candidates.length) return null;
    const order = rule === 'earliest' || rule === 'latest' ? rule : tie;
    if (['earliest', 'latest'].includes(order) && candidates.some(r => !Number.isFinite(r.modified))) return null;
    candidates.sort((a, b) => {
        const time = order === 'earliest' ? a.modified - b.modified : order === 'latest' ? b.modified - a.modified : 0;
        return time || naturalCompare(a.path, b.path) || a.id - b.id;
    });
    return candidates[0];
}
export async function hashFile(file, algorithm, { cancelled = () => false, progress = () => {} } = {}) {
    const check = () => { if (cancelled()) { const error = new Error('已停止'); error.name = 'AbortError'; throw error; } };
    check();
    if (algorithm === 'MD5') {
        const hash = new SparkMD5.ArrayBuffer();
        try {
            for (let offset = 0; offset < file.size; offset += 8 * 1024 * 1024) {
                check();
                hash.append(await file.slice(offset, offset + 8 * 1024 * 1024).arrayBuffer());
                check(); progress(Math.min(file.size, offset + 8 * 1024 * 1024));
                await new Promise(resolve => setTimeout(resolve, 0));
            }
            check(); return hash.end();
        } finally { hash.destroy(); }
    }
    if (!['SHA-1', 'SHA-256'].includes(algorithm)) throw new Error('不支持的算法');
    if (!globalThis.crypto?.subtle) throw new Error('SHA 算法需要 HTTPS 或 localhost，请改用 MD5');
    if (file.size > 256 * 1024 * 1024) throw new Error('SHA 单文件上限 256 MB，大文件请改用分块 MD5');
    const data = await file.arrayBuffer(); check();
    const digest = await globalThis.crypto.subtle.digest(algorithm, data); check(); progress(file.size);
    return [...new Uint8Array(digest)].map(b => b.toString(16).padStart(2, '0')).join('');
}
