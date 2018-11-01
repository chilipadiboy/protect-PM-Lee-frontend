import React, { Component } from 'react';
import { matchPath } from 'react-router';
import { Layout, notification } from 'antd';
import { downloadFile } from '../../util/APIUtils';
import c3 from 'c3';
import 'c3/c3.css';

class DownloadCSV extends Component {
  constructor(props) {
      super(props);
      this.showOutput = this.showOutput.bind(this);
      this.b64DecodeUnicode = this.b64DecodeUnicode.bind(this);
      this.createGraph = this.createGraph.bind(this);
      this.parseData = this.parseData.bind(this);
  }

  b64DecodeUnicode(str) {
    return decodeURIComponent(atob(str).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
  }

  createGraph(data) {
    	var years = [];
    	var silverMinted = ["Silver Minted"];

    	for (var i = 1; i < data.length; i++) {
          if (data[i][0] !== undefined && data[i][0] !== null
              && data[i][2] !== undefined && data[i][2] !== null) {
              years.push(data[i][0]);
              silverMinted.push(data[i][2]);
          } else {
              years.push(0);
              silverMinted.push(0);
          }
    	}

    	var chart = c3.generate({
          bindto: '#chart',
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

  parseData(file) {
      var csvFilePath = file;
      var Papa = require("papaparse/papaparse.min.js");
  	  var results = Papa.parse(csvFilePath);
      this.createGraph(results.data);
  }

  showOutput(filename) {
      downloadFile(filename)
      .then(response => {
        this.parseData(this.b64DecodeUnicode(response));
      })
      .catch(error => {
        notification.error({
            message: 'Healthcare App',
            description: error.message || 'Sorry! Something went wrong. Please try again!'
        });
    });
  }

  componentDidMount() {
    const match = matchPath(this.props.history.location.pathname, {
      path: '/downloadCSV/:filename',
      exact: true,
      strict: false
    })
      const filename = match.params.filename;
      this.showOutput(filename);
      this.setState({filename});
  }

  render() {
    const { Header, Content } = Layout;

    return (
      <Layout className="layout">
        <Header>
          <div className="title">Generated Data</div>
        </Header>
        <Content>
          <div id="chart"></div>
        </Content>
      </Layout>
    );
  }
}

export default DownloadCSV;
