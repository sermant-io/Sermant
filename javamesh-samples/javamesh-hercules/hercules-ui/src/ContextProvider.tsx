import React, { createContext, useState } from "react"

const initAuth: string[] = []

const Context  = createContext({auth: initAuth, setAuth: function(value: string[]){}})

export function ContextProvider(props: { children: React.ReactNode }) {
    const [auth, setAuth] = useState(initAuth)
    return <Context.Provider value={{auth, setAuth}}>{props.children}</Context.Provider>
}

export default Context