import { CollectionTaskList } from '@/components/collection/CollectionTaskList';

export default function CollectionTasksPage() {
  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">催收任务管理</h1>
      <CollectionTaskList />
    </div>
  );
}
