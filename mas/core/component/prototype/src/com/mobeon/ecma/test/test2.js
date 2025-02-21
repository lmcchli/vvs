function dinMamma() { return "och jag";}

function foo() {
    var a = new Object();
    a.dinMamma = dinMamma;
    a.banan = 4711;

    return a;
}


function bar(a) {
    print (a.dinMamma());
}



