import React from "react";
import PropTypes from "prop-types";
import {Table} from "react-bootstrap";
import numeral from "numeral";
import QueryResultSetMetrics from "./queryResultSetMetrics";

class QueryResultSet extends React.Component {

    render() {
        const {queryUid, resultSet} = this.props;
        const {resultIndex, resultName, columnNames, rowSet} = resultSet;
        return (
            <div className="result-group">
                <h4>Query {resultIndex}: {resultName}</h4>
                <QueryResultSetMetrics {...{queryUid, resultSet}} />
                <Table striped bordered hover size="sm">
                    <thead>
                    <tr>
                        <th style={{width: '3em'}}>Row</th>
                        {columnNames.map(col => <th key={col} className="text-center">{col}</th>)}
                    </tr>
                    </thead>
                    <tbody>{rowSet.map(({cells = []}, i) => (
                        <tr key={i}>
                            <td>{i}</td>
                            {cells.map(this.renderCell)}
                        </tr>
                    ))}
                    </tbody>
                </Table>
            </div>
        );
    };

    renderCell(cell, i) {
        if (cell.isNan) {
            return <td key={i} className="nan">NaN</td>;
        }
        if (cell.isNull) {
            return <td key={i} className="null">NULL</td>;
        }
        switch (cell.bName.toLowerCase()) {
            case 'boolean':
                return <td key={i}>{cell.bData.toString()}</td>;
            case 'byte':
                return <td key={i}>{numeral(cell.bData).format('000')}</td>;
            case 'short':
                return <td key={i}>{numeral(cell.bData).format('0,0')}</td>;
            case 'integer':
                return <td key={i}>{numeral(cell.bData).format('0,0')}</td>;
            case 'long':
                return <td key={i}>{numeral(cell.bData).format('0,0')}</td>;
            case 'double':
                return <td key={i}>{numeral(cell.bData).format('0.00e+0')}</td>;
            case 'string':
                return <td key={i} className="string">{cell.bData}</td>;
            default:
                return <td key={i}>!!{cell.bData}!!</td>;
        }
    };
}

QueryResultSet.propTypes = {
    resultSet: PropTypes.object
};

export default QueryResultSet
