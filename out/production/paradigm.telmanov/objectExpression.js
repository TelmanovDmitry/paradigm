var VARIABLES = {
    "x" : 0,
    "y" : 1,
    "z" : 2
};

var ExpressionPrototype = {
    evaluate: function() {
        return this.apply(arguments);
    }
};

function AbstractOperation(operation, symbol, operands, num) {
    this.operands = operands;
    this.symbol = symbol;
    this.operation = operation;
    this.operandsNum = num;
}

AbstractOperation.prototype = Object.create(ExpressionPrototype);

AbstractOperation.prototype.toString = function() {
    var operationToString = "";
    for (var i = 0; i < this.operandsNum; i++) {
        operationToString += this.operands[i].toString() + " ";
    }
    operationToString += this.symbol;
    return operationToString;
};

AbstractOperation.prototype.apply = function(variables) {
    var args = [];
    for (var i = 0; i < this.operandsNum; i++) {
        args.push(this.operands[i].apply(variables));
    }
    return this.operation(args);
};

function createNewOperation(operation, symbol, operandsNum) {
    function Operation() {
        return new AbstractOperation(operation, symbol, arguments, operandsNum);
    }
    Operation.prototype = Object.create(AbstractOperation.prototype);
    return Operation;
}

var Sqrt = createNewOperation(function (args) { return Math.sqrt(Math.abs(args[0])); }, "sqrt", 1);

var Square = createNewOperation(function (args) { return args[0] * args[0]; }, "square", 1);

var Negate = createNewOperation(function (args) { return -args[0]; }, "negate", 1);

var Subtract = createNewOperation(function(args) { return args[0] - args[1]; }, "-", 2);

var Add = createNewOperation(function(args) { return args[0] + args[1]; }, "+", 2);

var Multiply = createNewOperation(function(args) { return args[0] * args[1]; }, "*", 2);

var Divide = createNewOperation(function(args) { return args[0] / args[1]; }, "/", 2);

var Min3 = createNewOperation(function(args) { return Math.min(args[0], args[1], args[2]); }, "min3", 3);

var Max5 = createNewOperation(function(args) { return Math.max(args[0], args[1], args[2], args[3], args[4]); }, "max5", 5);

function Const(value) {
    this.value = value;
}
Const.prototype = Object.create(ExpressionPrototype);
Const.prototype.apply = function() { return this.value; };
Const.prototype.toString = function() { return this.value.toString(); };

function Variable(name) {
    this.name = name;
}
Variable.prototype = Object.create(ExpressionPrototype);
Variable.prototype.apply = function(variables) { return variables[VARIABLES[this.name]]; };
Variable.prototype.toString = function() { return this.name; };