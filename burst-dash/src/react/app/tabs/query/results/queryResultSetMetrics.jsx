import React from "react";
import PropTypes from "prop-types";
import {commaNumber} from "../../../utility/burst-conversions";
import {CSVLink} from "react-csv";
import numeral from "numeral";
import {SingleRowTable} from "./helpers";
import {LabeledItem} from "../../../utility/helpers";

class QueryResultSetMetrics extends React.Component {
    render() {
        const {queryUid, resultSet} = this.props;

        return (
            <div className="w-100 bg-medium p-2">
                <SingleRowTable>
                    <LabeledItem name="index" value={resultSet.resultIndex}/>
                    <LabeledItem name="name" value={resultSet.resultName}/>
                    <LabeledItem name="rows" value={commaNumber(resultSet.metrics.rowCount)}/>
                    <LabeledItem name="succeed"
                                 value={<input type="checkbox" disabled checked={resultSet.metrics.succeeded}/>}/>
                    <LabeledItem name="limit"
                                 value={<input type="checkbox" disabled checked={resultSet.metrics.limited}/>}/>
                    <LabeledItem name="overflow"
                                 value={<input type="checkbox" disabled checked={resultSet.metrics.overflowed}/>}/>
                    <td className="pl-5">
                        <CSVLink data={this.onExportResults(resultSet)} filename={`${queryUid}_${resultSet.resultIndex}.csv`}>
                            <span className="fa fa-file-excel-o"/>&nbsp;&nbsp;
                            <span style={{fontWeight: 'bold', color: 'steelblue'}}>Export CSV</span>
                        </CSVLink>
                    </td>
                </SingleRowTable>
            </div>
        );

    }

    /**
     *
     * @param resultSet
     * @param event
     * @returns {Array}
     */
    onExportResults = (resultSet, event) => {
        let resultData = [];
        let columnNames = [];
        resultSet.columnNames.map(cn => {
            columnNames.push(cn)
        });
        resultData.push(columnNames);
        resultSet.rowSet.map(row => {
            let rowData = [];
            row.cells.map(cell => {
                if (cell.isNan) {
                    rowData.push('NaN')
                } else if (cell.isNull) {
                    rowData.push('NULL')
                } else {
                    switch (cell.bType) {
                        case 0: {
                            rowData.push(cell.bData);
                            break;
                        }
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5: {
                            rowData.push(numeral(cell.bData).value());
                            break;
                        }
                        case 6: {
                            rowData.push(cell.bData);
                            break;
                        }
                        default: {
                            rowData.push(cell.bData);
                        }
                    }
                }
            });
            resultData.push(rowData)
        });
        return resultData;
    };


}

QueryResultSetMetrics.propTypes = {
    resultSet: PropTypes.object
};

export default QueryResultSetMetrics
