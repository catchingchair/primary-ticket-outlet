import { createContext } from 'react';
import { useAuthSession } from '../hooks/useAuthSession';

const AuthContext = createContext(undefined);

export function AuthProvider({ children }) {
  const session = useAuthSession();
  return <AuthContext.Provider value={session}>{children}</AuthContext.Provider>;
}

export default AuthContext;
export { AuthContext };
