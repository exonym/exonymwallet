import 'package:flutter/cupertino.dart';
import 'search_bar.dart';
import 'styles.dart';

class ProofTab extends StatefulWidget {
  const ProofTab({super.key});

  @override
  State<ProofTab> createState() {
    return _ProofTabState();
  }
}

class _ProofTabState extends State<ProofTab> {
// ...

  late final TextEditingController _controller;
  late final FocusNode _focusNode;
  String _terms = '';

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController()..addListener(_onTextChanged);
    _focusNode = FocusNode();
  }

  @override
  void dispose() {
    _focusNode.dispose();
    _controller.dispose();
    super.dispose();
  }

  void _onTextChanged() {
    setState(() {
      _terms = _controller.text;
    });
  }

  Widget _buildSearchBox() {
    return Padding(
      padding: const EdgeInsets.all(8),
      child: SearchBar(
        controller: _controller,
        focusNode: _focusNode,
      ),
    );
  }    // TO HERE

  @override
  Widget build(BuildContext context) {
    // final model = Provider.of<AppStateModel>(context);

    return DecoratedBox(
      decoration: const BoxDecoration(
        color: Styles.scaffoldBackground,
      ),
      child: SafeArea(
        child: Column(
          children: [
            _buildSearchBox(),
            Expanded(
              child: SingleChildScrollView(
                child: CupertinoListSection(
                  topMargin: 0,
                  children: const [Text("Wow")],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // List<ProductRowItem> _buildProductRowItem(List<Product> results) {
  //   if (results.isEmpty) {
  //     return [ const ProductRowItem(identity: Product(category: Category.all, id: 0, isFeatured: false, name: "No Results", price: 0)) ];
  //   } else {
  //     return [
  //       for (var product in results)
  //         ProductRowItem(
  //           identity: product,
  //         )
  //     ];
  //   }
  // }
}