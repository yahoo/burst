import React from 'react'
import cn from "classnames";

export class Pane extends React.Component {
    static Fixed = ({id = '', className = '', overflow = false, minHeight, maxHeight}) => null;
    static Flex = ({id = '', className = ''}) => null;

    render() {
        const {id, children, noFixedHeight = false} = this.props;
        const toRender = children instanceof Array ? children.filter(c => c) : [children];
        const numFlexPanes = toRender.filter(c => c.type === Pane.Flex).length;
        const prop = 'height';
        const flexStyle = {[prop]: `${Math.floor(100 / numFlexPanes)}%`};
        return (
            <div id={id} className="b-flex-col">
                {toRender.map((c, i) => {
                    if (c.type === Pane.Fixed) {
                        const {id, className, overflow, minHeight, maxHeight} = c.props;
                        return (
                            <div key={i} id={id}
                                 className={cn('b-pane', className, {'b-overflow': overflow})}
                                 style={{minHeight, maxHeight}}>
                                {c.props.children}
                            </div>
                        );
                    } else if (c.type === Pane.Flex) {
                        const {className, id} = c.props;
                        return (
                            <div key={i} id={id}
                                 className={cn('b-pane flex-grow-1', className)}
                                 style={noFixedHeight ? undefined : flexStyle}>
                                {c.props.children}
                            </div>
                        );
                    }
                    throw new Error(`Unknown child type ${c.type}`);
                })}
            </div>
        )
    }
}
