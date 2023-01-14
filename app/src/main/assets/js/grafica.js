

var ctx = document.getElementById('graph');
var myChart = new Chart(ctx,{ 
    type: 'bar',
    //data: data,
    options: {
        scales:{ 
            yAxes:[{ 
                ticks: { 
                    beginAtZero:true 
                } 
            }] 
        } 
    }
});

const labels = ['00h', '08h', '16h', '24h'];
 

const dataset4 = {
    label: "Concentración sin riesgo",
    data: [0, 0, 7, 0],
    borderColor: 'rgb(111,245,0.8)',
    fill: false,
    tension: 0.1
};

const dataset2 = {
    label: "Concentración elevada",
    data: [0, 10, 0, 0],
    borderColor: 'rgb(247,126,0.8)',
    fill: false,
    tension: 0.1
};

const dataset1 = {
    label: "Peligro",
    data: [0, 0, 0, 20],
    borderColor: 'rgba(248, 37, 37, 0.8)',
    fill: false,
    tension: 0.1
};
 
 
const graph = document.querySelector("#grafica");
 
const data = {
    labels: labels,
    datasets: [dataset1,dataset2,dataset4]
};
 
const config = {
    type: 'bar',
    data: data,
};
 
new Chart(graph, config);