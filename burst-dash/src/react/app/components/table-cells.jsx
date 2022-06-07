import React from 'react';
import {dateTime, hhmmssFormat} from "../utility/burst-conversions";

export const ColSort = ({column}) => <span>{column.isSorted ? column.isSortedDesc ? ' 🔽' : ' 🔼' : ''}</span>

export const GuidCell = ({value}) => <code title={value}>{value}</code>

export const DateCell = ({value}) => <span title={dateTime(value)} style={{whiteSpace: 'nowrap'}}>{dateTime(value, hhmmssFormat)}</span>

export const TextFilter = ({column: {filterValue, setFilter}}) =>
    <input className="form-control form-control-sm" type="text" value={filterValue || ''}
           onChange={e => setFilter(e.target.value || undefined)}/>

/**
 * Create a new react table component for filtering from a list of values
 * @param options the list of values, either string literals or objects with keys: value, title
 * @param displayName the display displayName of the component
 * @param unfilteredTitle the text for an empty, unfiltered selection
 * @returns {function({column: {filterValue: *, setFilter: *, width: *}})} A new react component
 */
export const makeFilterCell = (options = [], displayName = 'Filter', unfilteredTitle = "All") => {
    const opts = options.map(opt =>
        opt.value ? <option key={opt.value} value={opt.value}>{opt.title}</option>
            : <option key={opt}>{opt}</option>
    )
    const Component = ({column: {filterValue, setFilter}}) => (
        <select value={filterValue} className="form-control form-control-sm" style={{width: '100%'}}
                onChange={e => setFilter(e.target.value || undefined)}>
            <option value="">{unfilteredTitle}</option>
            {opts}
        </select>
    )
    Component.displayName = displayName;
    return Component
}

