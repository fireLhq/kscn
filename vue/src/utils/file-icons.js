const ICON_BY_EXTENSION = {
    ai: "adobe_ai",
    eps: "adobe_eps",
    pdf: "adobe_pdf",
    psd: "adobe_psd",

    apk: "app_android",
    aab: "app_android",
    xapk: "app_android",
    apks: "app_android",

    exe: "app_windows",
    msi: "app_windows",
    appx: "app_windows",
    appxbundle: "app_windows",
    msix: "app_windows",
    msixbundle: "app_windows",

    dmg: "app_mac",
    pkg: "app_mac",
    app: "app_mac",

    deb: "app_linux",
    rpm: "app_linux",
    appimage: "app_linux",
    snap: "app_linux",
    flatpak: "app_linux",

    bin: "binary",
    hex: "binary",
    img: "binary",
    iso: "binary",
    dat: "binary",

    php: "code",
    go: "code",
    rs: "code",
    kt: "code",
    kts: "code",
    swift: "code",
    rb: "code",
    pl: "code",
    pm: "code",
    lua: "code",
    r: "code",
    rmd: "code",
    dart: "code",
    scala: "code",
    groovy: "code",
    asm: "code",
    s: "code",
    pas: "code",
    dpr: "code",
    f: "code",
    f90: "code",
    for: "code",
    m: "code",
    mm: "code",
    vb: "code",

    zip: "compress",
    rar: "compress",
    "7z": "compress",
    tar: "compress",
    gz: "compress",
    bz2: "compress",
    xz: "compress",
    lz: "compress",
    lzma: "compress",
    zst: "compress",
    tgz: "compress",
    tbz: "compress",
    tbz2: "compress",
    txz: "compress",
    cab: "compress",
    arj: "compress",

    obj: "cube",
    stl: "cube",
    fbx: "cube",
    dae: "cube",
    glb: "cube",
    gltf: "cube",
    blend: "cube",
    "3ds": "cube",
    max: "cube",
    step: "cube",
    stp: "cube",
    iges: "cube",
    igs: "cube",

    json: "data_json",
    json5: "data_json",
    jsonc: "data_json",
    geojson: "data_json",

    yml: "data_yaml",
    yaml: "data_yaml",

    out: "executable",
    elf: "executable",
    o: "executable",
    objdump: "executable",
    obj: "executable",
    so: "executable",
    dylib: "executable",
    dll: "executable",
    a: "executable",
    lib: "executable",
    class: "executable",
    pyc: "executable",
    pyd: "executable",
    whl: "executable",
    jar: "executable",
    war: "executable",

    c: "lang_c",
    h: "lang_c",
    cpp: "lang_c",
    cxx: "lang_c",
    cc: "lang_c",
    hpp: "lang_c",
    hxx: "lang_c",
    hh: "lang_c",
    cs: "lang_c",

    java: "lang_java",

    js: "lang_javascript",
    jsx: "lang_javascript",
    ts: "lang_javascript",
    tsx: "lang_javascript",
    mjs: "lang_javascript",
    cjs: "lang_javascript",
    vue: "lang_javascript",

    py: "lang_python",
    pyw: "lang_python",
    pyi: "lang_python",
    ipynb: "lang_python",

    html: "markup_html",
    htm: "markup_html",
    xhtml: "markup_html",

    md: "markup_markdown",
    markdown: "markup_markdown",
    mdx: "markup_markdown",

    svg: "markup_svg",

    xml: "markup_xml",
    xsd: "markup_xml",
    xsl: "markup_xml",
    xslt: "markup_xml",
    wsdl: "markup_xml",

    ggb: "math",
    ggt: "math",
    mtt: "math",
    mat: "math",
    nb: "math",
    cdf: "math",
    cir: "math",
    asc: "math",
    sch: "math",
    brd: "math",
    sim: "math",
    shp: "math",
    dwg: "math",
    dxf: "math",

    mp3: "music",
    wav: "music",
    flac: "music",
    aac: "music",
    ogg: "music",
    opus: "music",
    m4a: "music",
    wma: "music",
    ape: "music",
    mid: "music",
    midi: "music",

    accdb: "office_accdb",
    mdb: "office_accdb",
    csv: "office_csv",
    doc: "office_doc",
    dot: "office_doc",
    docx: "office_docx",
    dotx: "office_docx",
    ppt: "office_ppt",
    pot: "office_ppt",
    pps: "office_ppt",
    pptx: "office_pptx",
    ppsx: "office_pptx",
    potx: "office_pptx",
    vsd: "office_vsd",
    vsdx: "office_vsd",
    xls: "office_xls",
    xlt: "office_xls",
    xlsx: "office_xlsx",
    xlsm: "office_xlsx",
    xltx: "office_xlsx",

    jpg: "picture",
    jpeg: "picture",
    png: "picture",
    gif: "picture",
    bmp: "picture",
    webp: "picture",
    tiff: "picture",
    tif: "picture",
    ico: "picture",
    heic: "picture",
    heif: "picture",
    avif: "picture",
    raw: "picture",
    cr2: "picture",
    nef: "picture",
    arw: "picture",

    sql: "query_sql",

    sh: "script",
    bash: "script",
    zsh: "script",
    fish: "script",
    ksh: "script",
    csh: "script",
    bat: "script",
    cmd: "script",
    ps1: "script",
    psm1: "script",
    vbs: "script",
    vbe: "script",
    command: "script",
    workflow: "script",

    css: "style_css",
    scss: "style_css",
    sass: "style_css",
    less: "style_css",
    styl: "style_css",
    pcss: "style_css",

    txt: "text",
    text: "text",
    log: "text",
    ini: "text",
    cfg: "text",
    conf: "text",
    config: "text",
    properties: "text",
    toml: "text",
    env: "text",
    rtf: "text",
    tex: "text",
    latex: "text",
    rst: "text",
    msg: "text",

    ai_outline: "vector",
    emf: "vector",
    wmf: "vector",
    cdr: "vector",
    sketch: "vector",
    fig: "vector",
    xd: "vector",
    epsf: "vector",

    mp4: "video",
    avi: "video",
    mov: "video",
    wmv: "video",
    flv: "video",
    mkv: "video",
    webm: "video",
    m4v: "video",
    mpg: "video",
    mpeg: "video",
    tsvideo: "video",
    mts: "video",
    m2ts: "video",
    rm: "video",
    rmvb: "video",
    "3gp": "video"
};

const TEXT_LIKE_NAMES = new Set(["license", "readme", "changelog", "makefile", "dockerfile"]);

export function getFileIconName(fileName, type) {
    if (type === "folder") {
        return "folder";
    }

    if (!fileName || typeof fileName !== "string") {
        return "other";
    }

    const normalizedName = fileName.trim().toLowerCase();
    if (!normalizedName) {
        return "other";
    }

    if (TEXT_LIKE_NAMES.has(normalizedName)) {
        return "text";
    }

    const lastDotIndex = normalizedName.lastIndexOf(".");
    if (lastDotIndex === -1 || lastDotIndex === normalizedName.length - 1) {
        return "other";
    }

    const ext = normalizedName.slice(lastDotIndex + 1);
    return ICON_BY_EXTENSION[ext] || "other";
}

export function getFileIconPath(iconName) {
    return require(`@/assets/images/components/icon/${iconName}.svg`);
}

