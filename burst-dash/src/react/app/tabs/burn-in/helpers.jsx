import React from "react";

const normalizeUnit = (unit = '') => {
    const normUnit = unit.toUpperCase();
    if (normUnit === 'minute'.toUpperCase()) {
        return 'minute'
    } else if (normUnit === 'minutes'.toUpperCase()) {
        return 'minutes'
    } else if (normUnit === 'hour'.toUpperCase()) {
        return 'hour'
    } else if (normUnit === 'hours'.toUpperCase()) {
        return 'hours'
    } else if (normUnit === 'day'.toUpperCase()) {
        return 'day'
    } else if (normUnit === 'days'.toUpperCase()) {
        return 'days';
    }
    throw new Error(`Unknown duration unit ${unit}`)
}

export const GrowingEditor = ({value, cols = 90, minRows = 4, readOnly = false, onChange}) =>
    <textarea rows={Math.max(minRows, (value.match(/\n/g) || []).length + 2)} cols={cols} value={value}
              readOnly={readOnly} onChange={onChange}/>

export const DurationPicker = ({value = "", readOnly = false, onChange = (num, unit) => null}) => {
    const [num, unitParse] = value.split(' ')
    const unit = normalizeUnit(unitParse)
    const suffix = num > 1 ? "s" : ""
    const changeNum = e => {
        const nextValue = +e.target.value;
        if (+num === 1) {
            onChange(nextValue, `${unit}s`);
        } else if (nextValue <= 1) {
            const newNum = nextValue < 1 ? 1 : nextValue;
            const newUnit = unit.substring(0, unit.length - 1);
            onChange(newNum, newUnit)
        } else {
            onChange(nextValue, unit)
        }
    }
    return (
        <div>
            <input type="number" value={num} min={1} onChange={changeNum} readOnly={readOnly}/>
            <select value={unit} onChange={e => onChange(num, e.target.value)} disabled={readOnly}>
                <option value={`minute${suffix}`}>Minute{suffix}</option>
                <option value={`hour${suffix}`}>Hour{suffix}</option>
                <option value={`day${suffix}`}>Day{suffix}</option>
            </select>
        </div>
    )
}
export const ConfigRow = ({label, className = "", children = [], show = true}) => {
    return show ? (
        <div className={`row config-row ${className}`}>
            <div className="col-sm-2">{label}</div>
            <div className="col">{children}</div>
        </div>
    ) : null
}

export const ConditionalInput = ({value, readOnly = false, onCheck = (checked = false) => null, children}) => (
    <>
        <input type="checkbox" disabled={readOnly} checked={!(value === null || value === undefined)}
               onChange={e => onCheck(e.target.checked)}/>
        {(value !== null && value !== undefined) && (
            <>
                <br/>
                {children}
            </>
        )}
    </>
)
export const MaxDurationInput = ({duration, readOnly, onCheck = e => null, onChange = (num, unit) => null}) => (
    <ConditionalInput value={duration} readOnly={readOnly} onCheck={onCheck}>
        <DurationPicker value={duration} readOnly={readOnly} onChange={onChange}/>
    </ConditionalInput>
)
export const LoadQueryInput = ({value, readOnly = false, onCheck = (e = false) => null, onChange = e => null}) => (
    <ConditionalInput value={value} readOnly={readOnly} onCheck={onCheck}>
        <GrowingEditor minRows={2} value={value} readOnly={readOnly} onChange={onChange}/>
    </ConditionalInput>
)
