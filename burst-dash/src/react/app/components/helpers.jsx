import React from 'react';

export const Row = ({label, value, labelTitle, labelClass, valueClass}) => (
    <tr>
        <Item label={label} labelTitle={labelTitle} labelClass={labelClass} value={value} valueClass={valueClass}/>
    </tr>
);

export const Item = ({label, value, labelTitle, labelClass = '', valueClass = ''}) => (
    <>
        <td className={`label ${labelTitle ? 'details' : ''} ${labelClass}`} title={labelTitle}>{label}</td>
        <td className={`value ${valueClass}`}>{value}</td>
    </>
);
