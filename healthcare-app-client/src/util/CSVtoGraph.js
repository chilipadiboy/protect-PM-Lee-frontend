import React, { Component } from 'react';
import './CSVtoGraph.css';
import Papa from 'papaparse';
import c3 from 'c3';
import * from 'd3';
import {XYPlot, XAxis, YAxis, VerticalGridLines, HorizontalGridLines, LineSeries} from 'react-vis';

const DATA_URL = "../sample-data/sample.csv";

function createGraph(data) {
  	var years = [];
  	var silverMinted = ["Silver Minted"];

  	for (var i = 1; i < data.length; i++) {
  		years.push(data[i][0]);
  		silverMinted.push(data[i][2]);
  	}

  	console.log(years);
  	console.log(silverMinted);

  	var chart = c3.generate({
  	    data: {
  	        columns: [
  	        	silverMinted
  	        ]
  	    },
  	    axis: {
  	        x: {
  	            type: 'category',
  	            categories: years,
  	            tick: {
  	            	multiline: false,
                  	culling: {
                      	max: 15
                  	}
              	}
  	        }
  	    },
  	    zoom: {
          	enabled: true
      	},
  	    legend: {
  	        position: 'right'
  	    }
  	});
}

function parseData() {
	  Papa.parse(DATA_URL, {
		    download: true,
		    complete: function(results) {
			       createGraph(results.data);
		    }
	  });
}

class Chart extends Component {
    constructor(props) {
        super(props)
        this.state = {
            results: [],
        };
    }

    /* componentDidMount() {
        fetch(API_URL)
            .then(response => {
                if (response.ok) {
                    return  response.json()
                }
                else {
                    throw new Error ('something went wrong')
                }
            })
            .then(response => this.setState({
                results: response.results.filter((r)=>{
                        return r.name === 'JavaScript';
                    })
                })
            )} */

    render() {
      const {results} = this.state;
      return (
            <parseData />
            // <XYPlot
            //     width={1000}
            //     height={500}>
            //     <VerticalGridLines />
            //     <HorizontalGridLines />
            //     <XAxis />
            //     <YAxis />
            //     <LineSeries
            //         data={[
            //             {x: 1, y: 4},
            //             {x: 5, y: 2},
            //             {x: 15, y: 6}
            //         ]}/>
            // </XYPlot>
        );
      /* const Chart = (props) => {

        const dataArr = props.data.map((d)=> {
          return {x: d.year + '/' + d.quarter,
          y: parseFloat(d.count/1000)}
        });

        return (
          <XYPlot
            xType="ordinal"
            width={1000}
            height={500}>
            <VerticalGridLines />
            <HorizontalGridLines />
            <XAxis title="X" />
            <YAxis title="Y" />
                <LineSeries
                    data={dataArr}
                    style={{stroke: 'violet', strokeWidth: 3}}/>
                    </XYPlot>
                  );
                } */
      }
}

export default Chart;
