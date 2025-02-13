import { UserAccount, User, Transaction, Card } from '../../types';

const myInit = (method = 'GET', token?: string) => {
  return {
    method,
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : '',
    },
    mode: 'cors' as RequestMode,
    cache: 'default' as RequestCache,
  };
};

const myRequest = (endpoint: string, method: string, token?: string) =>
  new Request(endpoint, myInit(method, token));

const baseUrl = 'http://localhost:3500';

const rejectPromise = (response?: Response): Promise<Response> =>
  Promise.reject({
    status: (response && response.status) || '00',
    statusText: (response && response.statusText) || 'Ocurrió un error',
    err: true,
  });

  export const login = (email: string, password: string) => {
    return fetch(myRequest(`${baseUrl}/api-usuario/login`, 'POST'), {
      body: JSON.stringify({ email, password }),
    })
      .then((response) => {
        if (response.ok) {
          return response.json();
        }
        return rejectPromise(response);
      })
      .catch((err) => {
        console.log(err);
        return rejectPromise(err);
      });
  };

//Improved typing
export const createAnUser = (user: Omit<User, 'id'>) => {
  return fetch(myRequest(`${baseUrl}/api-usuario/register`, 'POST'), {
    body: JSON.stringify(user),
  })
    .then((response) => {
      if (response.ok) {
        // SUCCESSFUL response (2xx status code)
        return response.json(); // Parse the JSON body
      } else {
        // ERROR response (non-2xx status code)
        // Attempt to parse the JSON error response, if it exists.
        return response.json() // Important!  Try to get JSON error body
            .then(errorData => {
                // If we *could* parse the JSON, reject with that data
                return Promise.reject(errorData); // Reject with the parsed error object
            })
            .catch(() => {
              // If parsing the JSON *failed*, reject with a generic error
              return rejectPromise(response);
            });
      }
    })
    .catch((err) => {
      // This catches *network* errors AND rejections from the .then block above
      console.error("Error creating user:", err);
      return Promise.reject(err); // Re-reject to propagate to the caller
    });
};



export const getUser = (id: string): Promise<User> => {
  return fetch(myRequest(`${baseUrl}/api-usuario/users/${id}`, 'GET'))
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

//Improved typing
export const updateUser = (
  id: string,
  data: Partial<User>,  // Partial<User> allows updating only some fields
  token: string
): Promise<Response> => {
  return fetch(myRequest(`${baseUrl}/api-usuario/users/${id}`, 'PATCH', token), {
    body: JSON.stringify(data),
  })
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};




export const createAnAccount = (userId: string, accessToken: string): Promise<Response> => {

  return fetch(
    myRequest(`${baseUrl}/api-cuenta/users/${userId}/accounts`, 'POST', accessToken),
    {

    }
  ).then((response) =>
    response.ok ? response.json() : rejectPromise(response)
  );
};

export const getAccount = (id: string, token: string): Promise<UserAccount> => {
  return fetch(myRequest(`${baseUrl}/api-cuenta/users/${id}/accounts`, 'GET', token), {})
    .then((response) => {
      if (response.ok) {
        return response.json().then((account) => account[0]);
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getAccounts = (): Promise<UserAccount[]> => {
  return fetch(myRequest(`${baseUrl}/api-cuenta/accounts`, 'GET'))
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const updateAccount = (
  id: string,
  data: any,
  token: string
): Promise<Response> => {
  return fetch(myRequest(`${baseUrl}/api-cuenta/users/${id}/accounts/1`, 'PATCH', token), {
    body: JSON.stringify(data),
  })
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUserActivities = (
  userId: string,
  token: string,
  limit?: number
): Promise<Transaction[]> => {
  return fetch(
    myRequest(
      `${baseUrl}/api-transaccion/users/${userId}/activities${limit ? `?_limit=${limit}` : ''}`,
      'GET',
      token
    )
  )
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUserActivity = (
  userId: string,
  activityId: string,
  token: string
): Promise<Transaction> => {
  return fetch(
    myRequest(
      `${baseUrl}/api-transaccion/users/${userId}/activities/${activityId}`,
      'GET',
      token
    )
  )
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUserCards = (
  userId: string,
  token: string
): Promise<Card[]> => {
  return fetch(myRequest(`${baseUrl}/api-cards/users/${userId}/cards`, 'GET', token))
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUserCard = (userId: string, cardId: string): Promise<Card> => {
  return fetch(myRequest(`${baseUrl}/api-cards/users/${userId}/cards/${cardId}`, 'GET'))
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const deleteUserCard = (
  userId: string,
  cardId: string,
  token: string
): Promise<Response> => {
  return fetch(
    myRequest(`${baseUrl}/api-cards/users/${userId}/cards/${cardId}`, 'DELETE', token)
  )
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const createUserCard = (
  userId: string,
  card: any,
  token: string
): Promise<Response> => {
  return fetch(myRequest(`${baseUrl}/api-cards/users/${userId}/cards`, 'POST', token), {
    body: JSON.stringify(card),
  })
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

// TODO: edit when backend is ready
export const createDepositActivity = (
  userId: string,
  amount: number,
  token: string
) => {

  const activity = {
    amount,
    type: 'Deposit',
  };

  return fetch(
    myRequest(`${baseUrl}/api-transaccion/users/${userId}/activities`, 'POST', token),
    {
      body: JSON.stringify(activity),
    }
  )
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    // .then((data) => {
    //   depositMoney(data.amount, userId, token);
    // })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};



export const createTransferActivity = (
  userId: string,
  token: string,
  origin: string,
  destination: string,
  amount: number,
  name?: string
) => {
  return fetch(
    myRequest(`${baseUrl}/api-transaccion/users/${userId}/activities`, 'POST', token),
    {
      body: JSON.stringify({
        type: 'Transfer',
        amount,
        origin,
        destination,
      }),
    }
  )
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};


