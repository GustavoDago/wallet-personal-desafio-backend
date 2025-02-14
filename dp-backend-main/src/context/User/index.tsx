// src/context/User/index.tsx
import React, { createContext, useEffect, useReducer } from 'react';
import userReducer from './userReducer';
import { User } from '../../types';
import { useAuth, useLocalStorage } from '../../hooks';
import { getUser, parseJwt } from '../../utils';
//import { userActionTypes } from './types'; // Import action types (see below)
import { UNAUTHORIZED } from '../../constants/status';

export interface UserInfoState {
  user: User | null;
  loading: boolean;
}

const initialState: UserInfoState = {
  user: null,
  loading: true,
};

// Define your action types *explicitly* (better than strings)
export const userActionTypes = { // Export this for use in other files.
    SET_USER: 'SET_USER',
    SET_USER_LOADING: 'SET_USER_LOADING', // Add loading action type.
    LOGOUT: 'LOGOUT'
} as const; // 'as const' makes this a readonly object with string literal types


// Define a union type for all possible payload types
type ActionPayload = User | boolean | null | string; // Add other payload types as needed


interface Action {
  type: typeof userActionTypes[keyof typeof userActionTypes]; // Use the action types
  payload?: ActionPayload; // Use the union type for payload
}

export const userInfoContext = createContext<{
  user: User | null;
  loading: boolean;
  dispatch: React.Dispatch<Action>; // Use the correct Action type
}>({
  user: null, // Provide default values
  loading: false,
  dispatch: () => null,
});

const UserInfoProvider = ({ children }: { children: React.ReactNode }) => {
    const [state, dispatch] = useReducer(userReducer, initialState);
    const [token, setToken] = useLocalStorage('token');
    const { isAuthenticated, setIsAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      const token = window.localStorage.getItem('token');
      if (token) {
        const info = parseJwt(token);
        const userId = info && info.sub;
          if(userId){
            dispatch({ type: userActionTypes.SET_USER_LOADING, payload: true }); // Set loading to true
            getUser(userId, token)
            .then((res) => {
                dispatch({ type: userActionTypes.SET_USER, payload: res });
                dispatch({ type: userActionTypes.SET_USER_LOADING, payload: false });
            })
            .catch((error) => {
              if (error.status === UNAUTHORIZED) {
                setToken(null);
                setIsAuthenticated(false);
              }
              dispatch({ type: userActionTypes.SET_USER_LOADING, payload: false }); // Set loading to false on error
            });
          }

      } else {
        setIsAuthenticated(false);
      }
    }
  }, [dispatch, isAuthenticated, setIsAuthenticated, setToken]);

  return (
    <userInfoContext.Provider
      value={{ user: state.user, loading: state.loading, dispatch }}
    >
      {children}
    </userInfoContext.Provider>
  );
};

export default UserInfoProvider;