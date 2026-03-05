import { CollectionTaskDetail } from '@/components/collection/CollectionTaskDetail';

export default function CollectionTaskDetailPage({ 
  params 
}: { 
  params: { id: string } 
}) {
  return <CollectionTaskDetail taskId={parseInt(params.id)} />;
}
