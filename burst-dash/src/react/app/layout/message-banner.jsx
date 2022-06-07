import React, {useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
import {Alert, Overlay} from "react-bootstrap";
import {actions} from "../store/reducers/crosscutting";

const MessageBar = ({target}) => {
    const dispatch = useDispatch()
    const {message} = useSelector(({crosscutting}) => {
        return {
            message: crosscutting.message
        }
    })
    const clearNotification = () => dispatch(actions.clearNotification(0))
    useEffect(() => {
        if (message.message) {
            const promise = dispatch(actions.clearNotification())
            return () => {
                promise.abort()
            }
        }
    }, [message])
    if (!target || !message.message) {
        return null;
    }
    return (
        <Overlay target={target} show placement="bottom">
            {({placement, scheduleUpdate, arrowProps, outOfBoundaries, show, ...props}) => (
                <Alert variant={message.variant}
                       className="w-75 main-status"
                       x-placement={placement}
                       dismissible onClose={clearNotification}
                       {...props}>
                    {message.title && <Alert.Heading>{message.title}</Alert.Heading>}
                    {message.message}
                </Alert>
            )}
        </Overlay>
    )
}

export default MessageBar
