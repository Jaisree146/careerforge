import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 2, // 2 minutes cache validity
      gcTime: 1000 * 60 * 10,   // 10 minutes garbage collection
      retry: (failureCount, error) => {
        // Do not retry 401 or 404 errors
        if (error?.message?.includes('401') || error?.message?.includes('404')) {
          return false;
        }
        return failureCount < 2;
      },
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});
