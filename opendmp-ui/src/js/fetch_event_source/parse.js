"use strict";
var __await = (this && this.__await) || function (v) { return this instanceof __await ? (this.v = v, this) : new __await(v); }
var __asyncGenerator = (this && this.__asyncGenerator) || function (thisArg, _arguments, generator) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var g = generator.apply(thisArg, _arguments || []), i, q = [];
    return i = {}, verb("next"), verb("throw"), verb("return"), i[Symbol.asyncIterator] = function () { return this; }, i;
    function verb(n) { if (g[n]) i[n] = function (v) { return new Promise(function (a, b) { q.push([n, v, a, b]) > 1 || resume(n, v); }); }; }
    function resume(n, v) { try { step(g[n](v)); } catch (e) { settle(q[0][3], e); } }
    function step(r) { r.value instanceof __await ? Promise.resolve(r.value.v).then(fulfill, reject) : settle(q[0][2], r); }
    function fulfill(value) { resume("next", value); }
    function reject(value) { resume("throw", value); }
    function settle(f, v) { if (f(v), q.shift(), q.length) resume(q[0][0], q[0][1]); }
};
var __asyncValues = (this && this.__asyncValues) || function (o) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var m = o[Symbol.asyncIterator], i;
    return m ? m.call(o) : (o = typeof __values === "function" ? __values(o) : o[Symbol.iterator](), i = {}, verb("next"), verb("throw"), verb("return"), i[Symbol.asyncIterator] = function () { return this; }, i);
    function verb(n) { i[n] = o[n] && function (v) { return new Promise(function (resolve, reject) { v = o[n](v), settle(resolve, reject, v.done, v.value); }); }; }
    function settle(resolve, reject, d, v) { Promise.resolve(v).then(function(v) { resolve({ value: v, done: d }); }, reject); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getMessages = exports.getLines = exports.parseStream = void 0;
function parseStream(stream) {
    return getMessages(getLines(getBytes(stream)));
}
exports.parseStream = parseStream;
function getBytes(stream) {
    return __asyncGenerator(this, arguments, function* getBytes_1() {
        const reader = stream.getReader();
        let result;
        while (!(result = yield __await(reader.read())).done) {
            yield yield __await(result.value);
        }
    });
}
function getLines(iter) {
    return __asyncGenerator(this, arguments, function* getLines_1() {
        var e_1, _a;
        let buffer;
        let position;
        let fieldLength;
        let discardTrailingNewline = false;
        try {
            for (var iter_1 = __asyncValues(iter), iter_1_1; iter_1_1 = yield __await(iter_1.next()), !iter_1_1.done;) {
                const arr = iter_1_1.value;
                if (buffer) {
                    buffer = concat(buffer, arr);
                }
                else {
                    buffer = arr;
                    position = 0;
                    fieldLength = -1;
                }
                const bufLength = buffer.length;
                let lineStart = 0;
                while (position < bufLength) {
                    if (discardTrailingNewline) {
                        if (buffer[position] === 10) {
                            lineStart = ++position;
                        }
                        discardTrailingNewline = false;
                    }
                    let lineEnd = -1;
                    for (; position < bufLength && lineEnd === -1; ++position) {
                        switch (buffer[position]) {
                            case 58:
                                if (fieldLength === -1) {
                                    fieldLength = position - lineStart;
                                }
                                break;
                            case 13:
                                discardTrailingNewline = true;
                            case 10:
                                lineEnd = position;
                                break;
                        }
                    }
                    if (lineEnd === -1) {
                        break;
                    }
                    yield yield __await({
                        line: buffer.subarray(lineStart, lineEnd),
                        fieldLength,
                    });
                    lineStart = position;
                    fieldLength = -1;
                }
                if (lineStart === bufLength) {
                    buffer = undefined;
                }
                else if (lineStart) {
                    buffer = buffer.subarray(lineStart);
                    position -= lineStart;
                }
            }
        }
        catch (e_1_1) { e_1 = { error: e_1_1 }; }
        finally {
            try {
                if (iter_1_1 && !iter_1_1.done && (_a = iter_1.return)) yield __await(_a.call(iter_1));
            }
            finally { if (e_1) throw e_1.error; }
        }
    });
}
exports.getLines = getLines;
function getMessages(iter) {
    return __asyncGenerator(this, arguments, function* getMessages_1() {
        var e_2, _a;
        let message = {};
        const decoder = new TextDecoder();
        try {
            for (var iter_2 = __asyncValues(iter), iter_2_1; iter_2_1 = yield __await(iter_2.next()), !iter_2_1.done;) {
                const { line, fieldLength } = iter_2_1.value;
                if (!line.length) {
                    for (const _ in message) {
                        yield yield __await(message);
                        message = {};
                        break;
                    }
                }
                else if (fieldLength > 0) {
                    const field = decoder.decode(line.subarray(0, fieldLength));
                    let isNumber = false;
                    switch (field) {
                        case 'retry':
                            isNumber = true;
                        case 'data':
                        case 'event':
                        case 'id': {
                            const valueOffset = fieldLength + (line[fieldLength + 1] === 32 ? 2 : 1);
                            let value = decoder.decode(line.subarray(valueOffset));
                            if (isNumber) {
                                value = parseInt(value, 10);
                                if (isNaN(value)) {
                                    break;
                                }
                            }
                            message[field] = value;
                            break;
                        }
                    }
                }
            }
        }
        catch (e_2_1) { e_2 = { error: e_2_1 }; }
        finally {
            try {
                if (iter_2_1 && !iter_2_1.done && (_a = iter_2.return)) yield __await(_a.call(iter_2));
            }
            finally { if (e_2) throw e_2.error; }
        }
    });
}
exports.getMessages = getMessages;
function concat(a, b) {
    const res = new Uint8Array(a.length + b.length);
    res.set(a);
    res.set(b, a.length);
    return res;
}
//# sourceMappingURL=parse.js.map