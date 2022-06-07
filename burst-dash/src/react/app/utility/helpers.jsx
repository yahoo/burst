import React from "react";

export const LabeledItem = ({name, value, condensed = false}) => (
    <>
        <td className="burst-small-label">{name}:</td>
        <td>{value}</td>
        {!condensed && <td className="spacer"/>}
    </>
);
