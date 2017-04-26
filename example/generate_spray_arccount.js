var times = 100;
var nbNodes = 10000;
//var period = 140;
var period = 280;
var step = 1;

console.log(`# network size
SIZE `+nbNodes+`

# parameters of periodic execution
CYCLES `+ (120+period*times) +`

random.seed 1237567892
network.size SIZE
simulation.endtime CYCLES
simulation.logtime 1

################### protocols ===========================
protocol.link peersim.core.IdleProtocol

protocol.rps descent.spray.Spray
protocol.rps.delta 1
protocol.rps.step 1
protocol.rps.start 1
protocol.rps.linkable link
protocol.rps.fail 0.000

`);

controlname1 = "control.oA";

    console.log( controlname1 + ` descent.controllers.DynamicNetwork
`+ controlname1 + `.protocol rps
`+ controlname1 + `.FINAL
`+ controlname1 + `.step `+ step +`
`+ controlname1 + `.addingPerStep ` + Math.ceil(nbNodes/2/100) + `
`+ controlname1 + `.startAdd `+ 0 + `
`+ controlname1 + `.endAdd `+ 99 +`
`);



var round = 0;
for (var i = 0; i < times ; ++i){
    round = 120+i*period;
    controlname1 = "control.o"+(2*i);
    controlname2 = "control.o"+(2*i+1);
    
    console.log( controlname1 + ` descent.controllers.DynamicNetwork
`+ controlname1 + `.protocol rps
`+ controlname1 + `.FINAL
`+ controlname1 + `.step ` + step + `
`+ controlname1 + `.addingPerStep ` + Math.ceil(nbNodes/2/100) + `
`+ controlname1 + `.startAdd `+ round + `
`+ controlname1 + `.endAdd `+ (round + 99) +`

`+ controlname2 +  ` descent.controllers.DynamicNetwork
`+ controlname2 +  `.protocol rps
`+ controlname2 +  `.FINAL
`+ controlname2 +  `.step ` + step + `
`+ controlname2 +  `.removingPerStep  ` + Math.ceil(nbNodes/2/100) + `
`+ controlname2 +  `.startRem `+ (round + 140) +`
`+ controlname2 +  `.endRem  `+ (round + 239) +`
`);

    // 0 -> 49
    // 70 -> 119
};






console.log(`################### initialization ======================

init.sch CDScheduler
init.sch.protocol rps
init.sch.randstart

##### Controls #####


control.0 descent.observers.Observer
control.0.program descent.observers.PVarianceAndArcCount
control.0.protocol rps
control.0.FINAL
control.0.step 1`);
