import React from 'react';

export const FaIcon = ({icon, className = '', inheritColor = false}) =>
    <span className={`fa fa-${icon} ${className}`} style={inheritColor ? {color: 'inherit'} : undefined}/>;
