import React from 'react';
import {Link} from "react-router-dom";
import {useDispatch} from "react-redux";
import {actions as crosscutting} from "../store/reducers/crosscutting";

const OverLink = ({over}) => {
    const dispatch = useDispatch()
    const fetchCatalog = async () => {
        await dispatch(crosscutting.fetchDomain(over.domainKey))
        await dispatch(crosscutting.fetchView(over.viewKey))
        await dispatch(crosscutting.selectDataset({domain: {pk: over.domainKey}, view: {pk: over.viewKey}}))
    }
    return (
        <Link to={"/catalog"} onClick={fetchCatalog} title={`Domain: ${over.domainKey}, View: ${over.viewKey}`}>
            <span>{over.domainKey}→{over.viewKey}</span>
        </Link>
    )
}

export default OverLink
