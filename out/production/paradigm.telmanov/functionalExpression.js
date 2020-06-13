function apply_operation(operation, symbol) {
    return function () {
        var operands = arguments;
        return function () {
            var args = [];
            for (var i = 0; i < NUM_OF_OPERANDS[symbol]; i++) {
                args.push(operands[i].apply(null, arguments));
            }
            return operation(args);
        };
    };
}

var subtract = apply_operation(function (args) { return args[0] - args[1]; }, "-");
var add = apply_operation(function (args) { return args[0] + args[1]; }, "+");
var multiply = apply_operation(function (args) { return args[0] * args[1]; }, "*");
var divide = apply_operation(function (args) { return args[0] / args[1]; }, "/");
var negate = apply_operation(function (args) { return -args[0]; }, "negate");
var abs = apply_operation(function (args) { return Math.abs(args[0]); }, "abs");
var iff = apply_operation(function (args) { return args[0] >= 0 ? args[1] : args[2]; }, "iff");


var VARIABLES = {
    "x" : 0,
    "y" : 1,
    "z" : 2
};

for (var v in VARIABLES) {
    this[v] = variable(v);
}

function variable(val) {
    return function() {
        return arguments[VARIABLES[val]];
    };
}

var CONST = {
    "one" : 1,
    "two" : 2,
    "pi": Math.PI,
    "e": Math.E
};

for (var name in CONST) {
    this[name] = cnst(CONST[name]);
}

function cnst(num) {
    return function() {
        return num;
    }
}

var OPERATIONS = {
    "abs" : abs,
    "iff" : iff,
    "negate" : negate,
    "+" : add,
    "-" : subtract,
    "*" : multiply,
    "/" : divide
};

var NUM_OF_OPERANDS = {
    "abs" : 1,
    "negate" : 1,
    "iff" : 3,
    "+" : 2,
    "-" : 2,
    "*" : 2,
    "/" : 2
};

function parse(expr) {
    var tokens = expr.split(' ').filter(function (token) { return token.length > 0; });
    var stack = [];
    for (var i = 0; i < tokens.length; i++) {
        if (tokens[i] in CONST) {
            stack.push(cnst(CONST[tokens[i]]));
            continue;
        }
        if (tokens[i] in VARIABLES) {
            stack.push(variable(tokens[i]));
            continue;
        }
        if (tokens[i] in OPERATIONS) {
            var args = [];
            for (var j = 0; j < NUM_OF_OPERANDS[tokens[i]]; j++) {
                args.push(stack.pop());
                args.reverse();
            }
            stack.push(OPERATIONS[tokens[i]].apply(null, args));
            continue;
        }
        if (!isNaN(tokens[i])) {
            stack.push(cnst(parseInt(tokens[i])));
            continue;
        }
        throw "Wrong token!";
    }
    return stack.pop();
}