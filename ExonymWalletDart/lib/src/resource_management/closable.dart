abstract class AbstractResource {
  void dispose();

}

void useResource<T extends AbstractResource>(T resource, void Function(T) fn) {
  fn(resource);
  resource.dispose();
}
