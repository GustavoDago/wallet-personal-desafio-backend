// src/hooks/useUserInfo.tsx
import React, { createContext, useContext, useReducer, Dispatch } from 'react';
import { User } from '../types'; // Import your User type

// Define the shape of your user state
interface UserState {
    user: User | null;
    loading: boolean;
}

// Define the possible actions for your reducer
type Action =
    | { type: 'SET_USER'; payload: User | null }
    | { type: 'SET_LOADING'; payload: boolean };

// Define the initial state
const initialState: UserState = {
    user: null,
    loading: false,
};

// Create the reducer function
const userReducer = (state: UserState, action: Action): UserState => {
    switch (action.type) {
        case 'SET_USER':
            return { ...state, user: action.payload };
        case 'SET_LOADING':
            return { ...state, loading: action.payload };
        default:
            return state;
    }
};


// Define the context type.  It now includes dispatch.
interface UserInfoContextType {
    user: User | null;
    loading: boolean;
    dispatch: Dispatch<Action>;
}

// Create the context, with default values.
export const userInfoContext = createContext<UserInfoContextType>({
    user: null,
    loading: false,
    dispatch: () => { }, // Provide an empty function as default.  It will never actually be used,
});

// Create a provider component
export const UserInfoProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [state, dispatch] = useReducer(userReducer, initialState);

    return (
        <userInfoContext.Provider value={{ user: state.user, loading: state.loading, dispatch }}>
            {children}
        </userInfoContext.Provider>
    );
};


// Create your custom hook
export const useUserInfo = () => {
    const context = useContext(userInfoContext);
    if (!context) { // Add this check
        throw new Error('useUserInfo must be used within a UserInfoProvider');
    }
    return context;
};
