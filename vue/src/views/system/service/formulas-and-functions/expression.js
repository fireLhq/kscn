// A deliberately small arithmetic grammar: no eval, property access or assignment.
const functions = {
    sin: [Math.sin, 1], cos: [Math.cos, 1], tan: [Math.tan, 1],
    asin: [Math.asin, 1], acos: [Math.acos, 1], atan: [Math.atan, 1],
    sinh: [Math.sinh, 1], cosh: [Math.cosh, 1], tanh: [Math.tanh, 1],
    sqrt: [Math.sqrt, 1], abs: [Math.abs, 1], exp: [Math.exp, 1],
    ln: [Math.log, 1], log: [Math.log10, 1], log10: [Math.log10, 1],
    floor: [Math.floor, 1], ceil: [Math.ceil, 1], sign: [Math.sign, 1],
    pow: [Math.pow, 2], atan2: [Math.atan2, 2], min: [Math.min, -1], max: [Math.max, -1]
};
export function compileExpression(input) {
    if (typeof input !== 'string' || !input.trim() || input.length > 500) throw new Error('表达式不能为空，且最多 500 个字符');
    const text = input.replace(/×/g, '*').replace(/÷/g, '/').replace(/[−–]/g, '-').replace(/π/g, ' pi ').replace(/\*\*/g, '^');
    const tokens = []; let position = 0;
    const tokenPattern = /(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?|[a-zA-Z][a-zA-Z0-9]*|[+\-*/^(),!]/y;
    while (position < text.length) {
        if (/\s/.test(text[position])) { position++; continue; }
        tokenPattern.lastIndex = position; const match = tokenPattern.exec(text);
        if (!match) throw new Error(`无法识别第 ${position + 1} 个字符：${text[position]}`);
        tokens.push(match[0].toLowerCase()); position = tokenPattern.lastIndex;
        if (tokens.length > 256) throw new Error('表达式过于复杂');
    }
    let index = 0, depth = 0;
    const peek = () => tokens[index];
    const expect = value => { if (tokens[index++] !== value) throw new Error(`此处需要 ${value}`); };
    function atom() {
        if (++depth > 48) throw new Error('括号嵌套过深');
        let fn; const value = tokens[index++];
        if (value === '(') { fn = sum(); expect(')'); }
        else if (/^(?:\d|\.)/.test(value || '')) {
            const number = Number(value); if (!Number.isFinite(number)) throw new Error('数值超出范围'); fn = () => number;
        } else if (value === 'x' || value === 'y') fn = (x, y) => value === 'x' ? x : y;
        else if (value === 'pi' || value === 'e' || value === 'tau') { const number = value === 'e' ? Math.E : Math.PI * (value === 'tau' ? 2 : 1); fn = () => number; }
        else if (Object.prototype.hasOwnProperty.call(functions, value)) {
            expect('('); const args = [sum()];
            while (peek() === ',') { index++; args.push(sum()); }
            expect(')'); const [operation, arity] = functions[value];
            if ((arity > 0 && args.length !== arity) || args.length > 8) throw new Error(`${value} 的参数数量不正确`);
            fn = (x, y) => operation(...args.map(arg => arg(x, y)));
        } else throw new Error(`不支持的变量或函数：${value || '表达式不完整'}`);
        while (peek() === '!') {
            index++; const operand = fn;
            fn = (x, y) => { const n = operand(x, y); if (!Number.isInteger(n) || n < 0 || n > 170) return NaN; let result = 1; for (let j = 2; j <= n; j++) result *= j; return result; };
        }
        depth--; return fn;
    }
    function power() { const left = atom(); if (peek() !== '^') return left; index++; const right = unary(); return (x,y) => Math.pow(left(x,y),right(x,y)); }
    function unary() { if (peek() === '+' || peek() === '-') { const op = tokens[index++]; const right = unary(); return (x,y) => (op === '-' ? -1 : 1) * right(x,y); } return power(); }
    function product() {
        let left = unary();
        while (peek() === '*' || peek() === '/' || (peek() && /^(?:[a-z\d.(])/.test(peek()))) {
            const op = peek() === '*' || peek() === '/' ? tokens[index++] : '*'; const right = unary(), previous = left;
            left = (x,y) => op === '*' ? previous(x,y) * right(x,y) : previous(x,y) / right(x,y);
        }
        return left;
    }
    function sum() {
        let left = product();
        while (peek() === '+' || peek() === '-') { const op = tokens[index++], right = product(), previous = left; left = (x,y) => op === '+' ? previous(x,y) + right(x,y) : previous(x,y) - right(x,y); }
        return left;
    }
    const fn = sum(); if (index !== tokens.length) throw new Error(`多余的符号：${peek()}`);
    return fn;
}
export function compileCurve(expression, mode) {
    const parts = expression.split('=');
    if (parts.length > 2) throw new Error('一条表达式最多包含一个等号');
    if (mode === 'implicit') {
        const left = compileExpression(parts[0]), right = compileExpression(parts[1] === undefined ? '0' : parts[1]);
        return (x,y) => left(x,y) - right(x,y);
    }
    if (!['y', 'x'].includes(mode)) throw new Error('不支持的绘图类型');
    if (parts.length === 2 && parts[0].trim() !== mode) throw new Error(`此类型请填写 ${mode}=… 或仅填写右侧表达式`);
    const rhs = parts.length === 2 ? parts[1] : parts[0];
    const forbidden = mode === 'y' ? 'y' : 'x';
    if (new RegExp(`\\b${forbidden}\\b`, 'i').test(rhs)) throw new Error(`表达式右侧不能使用 ${forbidden}，此类方程请选择隐式方程`);
    return compileExpression(rhs);
}
