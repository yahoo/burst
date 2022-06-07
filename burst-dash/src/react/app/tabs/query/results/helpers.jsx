import React from "react";

export const SingleRowTable = ({className, children}) => (
    <table className={className}>
        <tbody>
        <tr>
            {children}
        </tr>
        </tbody>
    </table>
);
