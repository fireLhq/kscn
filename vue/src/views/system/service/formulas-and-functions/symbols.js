export const symbolGroups = [
    { name: '结构', symbols: [
        ['分数','\\frac{□}{y}'],['平方','{□}^{2}'],['上标','{□}^{n}'],['下标','{□}_{n}'],['根号','\\sqrt{□}'],['n 次根','\\sqrt[n]{□}'],
        ['括号','\\left(□\\right)'],['绝对值','\\left|□\\right|'],['求和','\\sum_{i=1}^{n}{□}'],['积分','\\int_{a}^{b}{□}\\,dx'],
        ['极限','\\lim_{x\\to 0}{□}'],['导数','\\frac{d}{dx}{□}'],['偏导','\\frac{\\partial □}{\\partial x}'],['向量','\\vec{□}'],
        ['矩阵','\\begin{pmatrix}□ & y \\\\ z & w\\end{pmatrix}'],['分段','f(x)=\\begin{cases}□ & x\\ge 0 \\\\ -x & x<0\\end{cases}'],['二项式','\\binom{n}{□}']
    ]},
    { name: '运算与关系', symbols: [
        ['+',' + '],['−',' - '],['×',' \\times '],['÷',' \\div '],['±',' \\pm '],['·',' \\cdot '],['=',' = '],['≠',' \\ne '],['≤',' \\le '],['≥',' \\ge '],['≈',' \\approx '],
        ['∞','\\infty'],['∈',' \\in '],['∪',' \\cup '],['∩',' \\cap '],['→',' \\to '],['⇒',' \\Rightarrow '],['∀','\\forall '],['∃','\\exists '],['ℝ','\\mathbb{R}'],
        ['sin','\\sin{□}'],['cos','\\cos{□}'],['tan','\\tan{□}'],['ln','\\ln{□}'],['log','\\log_{a}{□}'],['eⁿ','e^{□}']
    ]},
    { name: '希腊字母', symbols: [['α','\\alpha'],['β','\\beta'],['γ','\\gamma'],['δ','\\delta'],['ε','\\varepsilon'],['θ','\\theta'],['λ','\\lambda'],['μ','\\mu'],['π','\\pi'],['ρ','\\rho'],['σ','\\sigma'],['φ','\\varphi'],['ω','\\omega'],['Δ','\\Delta'],['Σ','\\Sigma'],['Ω','\\Omega']] }
];
export const formulaExamples = [
    ['一元二次方程求根', 'x=\\frac{-b\\pm\\sqrt{b^2-4ac}}{2a}'],
    ['定积分', '\\int_{0}^{\\pi}\\sin x\\,dx=2'],
    ['求和', '\\sum_{k=1}^{n}k=\\frac{n(n+1)}{2}'],
    ['分段函数', 'f(x)=\\begin{cases}x^2 & x\\ge 0 \\\\ -x & x<0\\end{cases}'],
    ['矩阵', 'A=\\begin{pmatrix}1 & 2 \\\\ 3 & 4\\end{pmatrix}']
];
export const plotSymbols = [['x','x'],['y','y'],['平方','(□)^2'],['根号','sqrt(□)'],['sin','sin(□)'],['cos','cos(□)'],['tan','tan(□)'],['绝对值','abs(□)'],['ln','ln(□)'],['log','log(□)'],['eⁿ','exp(□)'],['π','pi'],['=','=']];
