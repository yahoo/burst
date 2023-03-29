import React, {useState} from 'react'
import cn from "classnames";

export const Pane = ({id, children, noFixedHeight = false, onDrop}) => {
    const toRender = children instanceof Array ? children.filter(c => c) : [children];
    const unrenderable = toRender.filter(c => c.type !== Pane.Flex && c.type !== Pane.Fixed).map(c => c.type)
    if (unrenderable.length > 0) {
        throw new Error(`Unrenderable components [${unrenderable.join(', ')}]`)
    }
    const numFlexPanes = toRender.filter(c => c.type === Pane.Flex).length;
    const flexStyle = {height: `${Math.floor(100 / numFlexPanes)}%`};
    const [dragging, setDragging] = useState(false)
    return (
        <div id={id} className={cn("b-flex-col", {dragging})}
             onDragOver={e => {
                 setDragging(true);
                 e.preventDefault()
             }}
             onDragLeave={e => {
                 setDragging(false);
             }}
             onDrop={e => {
                 e.preventDefault();
                 setDragging(false);
                 onDrop(e)
             }}>
            {toRender.map((c, i) => {
                if (c.type === Pane.Fixed) {
                    return <Pane.Fixed key={i} {...c.props}/>
                } else if (c.type === Pane.Flex) {
                    return (
                        <Pane.Flex key={i} {...c.props} flexStyle={noFixedHeight ? undefined : flexStyle} />
                    );
                }
                throw new Error(`Unknown child type ${c.type}`);
            })}
        </div>
    )
}
Pane.Fixed = ({id = '', className = '', overflow = false, minHeight, maxHeight, children}) => (
    <div id={id} className={cn('b-pane', className, {'b-overflow': overflow})} style={{minHeight, maxHeight}}>
        {children}
    </div>
);
Pane.Flex = ({id = '', className = '', flexStyle = {}, children}) => (
    <div id={id} className={cn('b-pane flex-grow-1', className)} style={flexStyle}>
        {children}
    </div>
);
