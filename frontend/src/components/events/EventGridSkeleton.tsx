export function EventGridSkeleton({ count = 6 }: { count?: number }) {
  return (
    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3" aria-label="Loading events" aria-busy="true">
      {Array.from({ length: count }, (_, index) => (
        <div key={index} className="h-52 animate-pulse rounded-2xl border border-slate-200 bg-white p-5">
          <div className="h-5 w-3/4 rounded bg-slate-200" />
          <div className="mt-7 h-4 w-1/2 rounded bg-slate-100" />
          <div className="mt-3 h-4 w-1/3 rounded bg-slate-100" />
          <div className="mt-8 h-4 w-24 rounded bg-slate-200" />
        </div>
      ))}
    </div>
  )
}
