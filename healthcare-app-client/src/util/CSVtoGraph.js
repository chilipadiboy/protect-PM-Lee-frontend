import React from 'react';
import './CSVtoGraph.css';
import {XYPlot, XAxis, YAxis, VerticalGridLines, HorizontalGridLines, LineSeries} from 'react-vis';

const Chart = (props) => {
  return (
    <XYPlot
      width={300}
      height={300}>
      <HorizontalGridLines />
      <LineSeries
      color="red"
      data={[
        {x: 1, y: 10},
        {x: 2, y: 5},
        {x: 3, y: 15}
      ]}/>
      <XAxis title="X" />
      <YAxis />
      </XYPlot>
      );
    /*
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
            <XAxis title="Period of time(year and quarter)" />
            <YAxis title="Number of pull requests (thousands)" />
                <LineSeries
                    data={dataArr}
                    style={{stroke: 'violet', strokeWidth: 3}}/>
        </XYPlot>
    );
    */
}

export default Chart;
