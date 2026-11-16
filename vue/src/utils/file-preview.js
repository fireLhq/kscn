import MarkdownIt from 'markdown-it';

export const PREVIEW_LIMIT = 10 * 1024 * 1024;
export const TEXT_PAGE_SIZE = 50000;
const textExtensions = new Set('txt text log md markdown html htm xml json csv tsv yaml yml toml ini conf cfg properties java c cc cpp cxx h hpp hxx py js jsx ts tsx vue css scss less sass sql sh bash bat cmd ps1 go rs rb php kt kts swift scala lua r cs tex svelte ipynb gradle env'.split(' '));
const languages = { java: 'java', c: 'c', h: 'c', cc: 'cpp', cpp: 'cpp', cxx: 'cpp', hpp: 'cpp', hxx: 'cpp', py: 'python', js: 'javascript', jsx: 'javascript', ts: 'typescript', tsx: 'typescript', vue: 'xml', html: 'xml', htm: 'xml', xml: 'xml', svelte: 'xml', json: 'json', ipynb: 'json', md: 'markdown', markdown: 'markdown', yml: 'yaml', yaml: 'yaml', css: 'css', scss: 'scss', less: 'less', sql: 'sql', sh: 'bash', bash: 'bash', bat: 'dos', cmd: 'dos', ps1: 'powershell', go: 'go', rs: 'rust', rb: 'ruby', php: 'php', kt: 'kotlin', kts: 'kotlin', cs: 'csharp', r: 'r', swift: 'swift', scala: 'scala', lua: 'lua', ini: 'ini', properties: 'properties', toml: 'ini' };
const markdown = new MarkdownIt({ html: false, linkify: false, maxNesting: 20 });

export function previewType(name) {
    const lower = name.toLowerCase();
    const extension = lower.includes('.') ? lower.split('.').pop() : '';
    let kind = '';
    if ('png jpg jpeg gif webp bmp svg avif ico'.split(' ').includes(extension)) kind = 'image';
    else if ('mp4 webm ogv mov m4v mkv avi'.split(' ').includes(extension)) kind = 'video';
    else if ('mp3 wav ogg oga m4a aac flac opus'.split(' ').includes(extension)) kind = 'audio';
    else if (extension === 'pdf') kind = 'pdf';
    else if (['doc', 'docx'].includes(extension)) kind = 'word';
    else if (['xls', 'xlsx'].includes(extension)) kind = 'excel';
    else if (['ppt', 'pptx'].includes(extension)) kind = 'ppt';
    else if (textExtensions.has(extension) || ['dockerfile', 'makefile', 'license', 'readme', '.gitignore', '.env'].includes(lower)) kind = 'text';
    return { kind, extension, language: languages[extension] || 'plaintext', renderable: ['html', 'htm', 'md', 'markdown'].includes(extension) };
}

export function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
}

export function renderedPage(text, extension, name) {
    const body = ['md', 'markdown'].includes(extension) ? `<main class="markdown">${markdown.render(text)}</main>` : text;
    const policy = "default-src 'none'; script-src 'none'; style-src 'unsafe-inline'; img-src data:; connect-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none'";
    const document = `<meta charset="UTF-8"><meta http-equiv="Content-Security-Policy" content="${policy}"><style>body{font-family:system-ui,sans-serif;padding:20px;overflow-wrap:anywhere}img{max-width:100%}pre{white-space:pre-wrap}table{border-collapse:collapse}th,td{border:1px solid #ddd;padding:6px}</style>${body}`;
    // 用户内容只放在没有任何 sandbox 放行权限的 iframe 中，绝不写入主站DOM。
    return `<!doctype html><html><head><meta charset="UTF-8"><title>${escapeHtml(name)} — 渲染预览</title><style>html,body,iframe{margin:0;width:100%;height:100%;border:0}body{overflow:hidden}</style></head><body><iframe title="渲染预览" sandbox="" referrerpolicy="no-referrer" srcdoc="${escapeHtml(document)}"></iframe></body></html>`;
}
